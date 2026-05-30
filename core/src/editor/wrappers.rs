use std::{num::NonZeroU16, ops::Range};

use typst::{
    World, WorldExt,
    diag::{Severity, SourceDiagnostic, Tracepoint},
    ecow::{EcoVec, eco_format},
    syntax::{FileId, Span, Spanned, SyntaxError},
};

use super::{state::SourceContext, world::MnemoWorld};

boltffi::custom_type! {
    pub FileId,
    remote = FileId,
    repr = u16,
    into_ffi = |id: &FileId| id.into_raw().get(),
    try_from_ffi = |s| {
        NonZeroU16::new(s)
            .ok_or_else(|| boltffi::CustomTypeConversionError)
            .map(|nz| FileId::from_raw(nz))
     },
}

#[derive(Debug, Copy, Clone, Eq, PartialEq, Ord, PartialOrd, Hash)]
pub struct TypstFileId(pub(crate) FileId);

impl TypstFileId {
    pub fn new(id: FileId) -> Self {
        Self(id)
    }

    pub fn inner(&self) -> FileId {
        self.0
    }
}

#[derive(Debug)]
#[boltffi::data]
pub struct TypstDiagnostic {
    pub range: Range<usize>,
    pub severity: TypstDiagnosticSeverity,
    pub message: String,
    pub hints: Vec<String>,
}

impl TypstDiagnostic {
    pub fn from_errors(
        errors: EcoVec<SyntaxError>,
        context: &SourceContext,
        world: &MnemoWorld,
    ) -> Box<[Self]> {
        errors
            .into_iter()
            .flat_map(|error| {
                map_aux_span(error.span, true, &[], context, world).map(|range| TypstDiagnostic {
                    range,
                    severity: TypstDiagnosticSeverity::Error,
                    message: error.message.to_string(),
                    hints: error.hints.into_iter().map(|s| s.to_string()).collect(),
                })
            })
            .collect()
    }

    pub fn from_diagnostics(
        diagnostics: EcoVec<SourceDiagnostic>,
        context: &SourceContext,
        world: &MnemoWorld,
    ) -> Box<[Self]> {
        diagnostics
            .into_iter()
            .flat_map(|mut diagnostic| {
                if diagnostic.message == "failed to load file" {
                    let source = world.source(diagnostic.span.id().unwrap()).unwrap();
                    let text = source
                        .text()
                        .get(world.range(diagnostic.span).unwrap())
                        .unwrap();

                    diagnostic.message = eco_format!("failed to load file: {text}");
                }

                map_aux_span(
                    diagnostic.span,
                    diagnostic.severity == Severity::Error,
                    &diagnostic.trace,
                    context,
                    world,
                )
                .map(|range| TypstDiagnostic {
                    range,
                    severity: TypstDiagnosticSeverity::from_severity(diagnostic.severity),
                    message: diagnostic.message.to_string(),
                    hints: diagnostic
                        .hints
                        .into_iter()
                        .map(|s| s.v.to_string())
                        .collect(),
                })
            })
            .collect()
    }
}

pub fn map_main_span(
    span: Span,
    is_error: bool,
    trace: &[Spanned<Tracepoint>],
    context: &SourceContext,
    world: &MnemoWorld,
) -> Option<Range<usize>> {
    let mut main_range = if Some(context.main_id) == span.id() {
        world.range(span)
    } else {
        None
    };

    if main_range.is_none() {
        if !is_error {
            return None;
        }

        for tracepoint in trace {
            if main_range.is_some() {
                break;
            } else if Some(context.main_id) == tracepoint.span.id() {
                main_range = world.range(tracepoint.span)
            }
        }
    }

    main_range
}

pub fn map_aux_span(
    span: Span,
    is_error: bool,
    trace: &[Spanned<Tracepoint>],
    context: &SourceContext,
    world: &MnemoWorld,
) -> Option<Range<usize>> {
    let aux_source = context.aux_source(&world)?;

    let main_range = map_main_span(span, is_error, trace, context, world);

    let aux_range = if let Some(main_range) = main_range {
        let aux_start = context.map_main_to_aux_from_right(main_range.start);
        let aux_end = context.map_main_to_aux_from_left(main_range.end);

        aux_start..aux_end
    } else {
        if !is_error {
            return None;
        }

        0..aux_source.text().len()
    };

    let aux_lines = aux_source.lines();
    let aux_start_utf16 = aux_lines.byte_to_utf16(aux_range.start)?;
    let aux_end_utf16 = aux_lines.byte_to_utf16(aux_range.end)?;
    let aux_range_utf16 = aux_start_utf16..aux_end_utf16;

    Some(aux_range_utf16)
}

#[derive(Debug, Clone)]
#[boltffi::data]
pub enum TypstDiagnosticSeverity {
    Error,
    Warning,
    Info,
    Hint,
}

impl TypstDiagnosticSeverity {
    pub fn from_severity(severity: Severity) -> Self {
        match severity {
            Severity::Error => Self::Error,
            Severity::Warning => Self::Warning,
        }
    }
}

#[boltffi::data]
pub struct TypstHighlight {
    pub tag: String,
    pub range: Range<usize>,
}

#[boltffi::data]
pub enum TypstJump {
    File {
        // id: u64,
        offset: usize,
    },
    // Url(String),
    // Position(Position),
}

impl TypstJump {
    pub fn from_mapped(
        jump: typst_ide::Jump,
        context: &SourceContext,
        world: &MnemoWorld,
    ) -> Option<Self> {
        match jump {
            typst_ide::Jump::File(id, main_position) => {
                if id != context.main_id {
                    return None;
                }

                let aux_source = context.aux_source(&world)?;
                let aux_position = context.map_main_to_aux_from_right(main_position);
                let aux_position_utf16 = aux_source.lines().byte_to_utf16(aux_position)?;

                Some(Self::File {
                    // id: state.finish(),
                    offset: aux_position_utf16,
                })
            }
            typst_ide::Jump::Url(..) => None,
            typst_ide::Jump::Position(..) => None,
        }
    }
}

#[derive(Clone, Copy)]
#[boltffi::data]
pub enum TypstCompletionKind {
    Syntax,
    Func,
    Type,
    Param,
    Constant,
    Path,
    Package,
    Label,
    Font,
    Symbol,
}

#[boltffi::data]
pub struct TypstCompletion {
    kind: TypstCompletionKind,
    label: String,
    apply: Option<String>,
    detail: Option<String>,
}

impl From<typst_ide::Completion> for TypstCompletion {
    fn from(value: typst_ide::Completion) -> Self {
        Self {
            kind: match value.kind {
                typst_ide::CompletionKind::Syntax => TypstCompletionKind::Syntax,
                typst_ide::CompletionKind::Func => TypstCompletionKind::Func,
                typst_ide::CompletionKind::Type => TypstCompletionKind::Type,
                typst_ide::CompletionKind::Param => TypstCompletionKind::Param,
                typst_ide::CompletionKind::Constant => TypstCompletionKind::Constant,
                typst_ide::CompletionKind::Path => TypstCompletionKind::Path,
                typst_ide::CompletionKind::Package => TypstCompletionKind::Package,
                typst_ide::CompletionKind::Label => TypstCompletionKind::Label,
                typst_ide::CompletionKind::Font => TypstCompletionKind::Font,
                typst_ide::CompletionKind::Symbol(_) => TypstCompletionKind::Symbol,
            },
            label: value.label.to_string(),
            apply: value.apply.map(|s| s.to_string()),
            detail: value.detail.map(|s| s.to_string()),
        }
    }
}
