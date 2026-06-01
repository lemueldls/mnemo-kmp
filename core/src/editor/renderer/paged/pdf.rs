use typst::compile;
use typst_pdf::{PdfOptions, pdf};
use typst_syntax::FileId;

use crate::editor::{renderer::RenderTarget, state::EditorState, wrappers::EditorDiagnostic};

pub fn render_pdf(id: &FileId, state: &mut EditorState) -> RenderPdfResult {
    state.world.main_id = Some(*id);

    let mut ir = state.prelude(id, RenderTarget::Pdf);

    let context = state.source_context_map.get_mut(id).unwrap();
    let main_source = context.main_source_mut(&mut state.world).unwrap();
    let text = main_source.text().to_string();
    ir += &text;

    main_source.replace(&ir);

    state.world.insert_source(context.aux_id, text);
    state.world.aux_id = Some(context.aux_id);

    let compiled = compile(&state.world);
    let mut diagnostics =
        EditorDiagnostic::from_diagnostics(compiled.warnings, context, &state.world).into_vec();

    let bytes = match compiled.output {
        Ok(document) => {
            match pdf(&document, &PdfOptions::default()) {
                Ok(pdf) => Some(pdf),
                Err(source_diagnostics) => {
                    diagnostics.extend(EditorDiagnostic::from_diagnostics(
                        source_diagnostics,
                        context,
                        &state.world,
                    ));

                    None
                }
            }
        }
        Err(source_diagnostics) => {
            diagnostics.extend(EditorDiagnostic::from_diagnostics(
                source_diagnostics,
                context,
                &state.world,
            ));

            None
        }
    };

    RenderPdfResult { bytes, diagnostics }
}

/// Result of rendering a Typst document to PDF.
#[boltffi::data]
pub struct RenderPdfResult {
    /// The rendered PDF document, if successful.
    pub bytes: Option<Vec<u8>>,
    /// Diagnostics and warnings produced during rendering.
    pub diagnostics: Vec<EditorDiagnostic>,
}
