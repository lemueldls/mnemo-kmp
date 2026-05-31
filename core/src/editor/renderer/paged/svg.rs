use std::{collections::VecDeque, hash::BuildHasher, ops::Range};

use comemo::Prehashed;
use rustc_hash::FxBuildHasher;
use typst::layout::{Abs, Frame, Point, Size};
use typst_svg::svg_frame;
use typst_syntax::FileId;

use super::BoundFrameItem;
use crate::editor::{
    renderer::{
        RenderTarget,
        paged::{PagedRender, items::chunk_by_items},
    },
    state::EditorState,
    wrappers::EditorDiagnostic,
};

/// Renders SVG frames for each chunked item in a Typst document.
#[typst_macros::time]
pub fn render_svgs_by_items(
    id: &FileId,
    text: &str,
    prelude: &str,
    state: &mut EditorState,
) -> SvgRender {
    let PagedRender {
        chunks,
        tooltips,
        diagnostics,
        document,
    } = chunk_by_items(id, text, prelude, RenderTarget::Svg, state);

    let context = state.get_source_context_mut(id);

    let frames = if let Some(document) = &document {
        let document_width = document
            .pages
            .iter()
            .map(|page| page.frame.width())
            .max()
            .unwrap_or_default();

        chunks
            .into_iter()
            .map(|chunk| {
                let items = Prehashed::new(chunk.items);

                let width = Abs::pt(chunk.width);
                let height = Abs::pt(chunk.height);
                let x_offset = Abs::pt(chunk.x_offset);
                let y_offset = Abs::pt(chunk.y_offset);

                render_svg(
                    items,
                    chunk.range,
                    width,
                    height,
                    x_offset,
                    y_offset,
                    document_width,
                )
            })
            .collect()
    } else {
        Vec::new()
    };

    // let tooltips = if let Some(document) = &document {
    //     let document_width = document
    //         .pages
    //         .iter()
    //         .map(|page| page.frame.width())
    //         .max()
    //         .unwrap_or_default();

    //     tooltips
    //         .into_iter()
    //         .map(|chunk| {
    //             let items = Prehashed::new(chunk.items);

    //             let width = Abs::pt(chunk.width);
    //             let height = Abs::pt(chunk.height);
    //             let x_offset = Abs::pt(chunk.x_offset);
    //             let y_offset = Abs::pt(chunk.y_offset);

    //             render_svg(
    //                 items,
    //                 chunk.range,
    //                 width,
    //                 height,
    //                 x_offset,
    //                 y_offset,
    //                 document_width,
    //             )
    //         })
    //         .collect()
    // } else {
    //     Vec::new()
    // };

    let tooltips = tooltips
        .into_iter()
        .map(|chunk| {
            let items = Prehashed::new(chunk.items);

            let width = Abs::pt(chunk.width);
            let height = Abs::pt(chunk.height);
            let x_offset = Abs::pt(chunk.x_offset);
            let y_offset = Abs::pt(chunk.y_offset);

            render_svg(items, chunk.range, width, height, x_offset, y_offset, width)
        })
        .collect();

    // crate::log!("tooltips: {tooltips:#?}");

    context.paged_document = document;

    SvgRender {
        frames,
        tooltips,
        diagnostics,
    }
}

/// Renders a single SVG frame from a set of frame items and metadata.
#[comemo::memoize]
#[typst_macros::time]
fn render_svg(
    items: Prehashed<VecDeque<BoundFrameItem>>,
    range: Range<usize>,
    width: Abs,
    height: Abs,
    x_offset: Abs,
    y_offset: Abs,
    document_width: Abs,
) -> SvgRangedFrame {
    let hash = FxBuildHasher.hash_one(&items) as u32;

    let mut frame = Frame::soft(Size::new(document_width, height));
    frame.push_multiple(items.into_inner().into_iter().map(|block| {
        let point = block.point - Point::new(Abs::zero(), y_offset);

        (point, block.item)
    }));

    let svg = svg_frame(&frame);

    let width = width.to_pt();
    let height = height.to_pt();
    let x_offset = x_offset.to_pt();
    let y_offset = y_offset.to_pt();

    let render = SvgFrameRender {
        svg,
        width,
        height,
        x_offset,
        y_offset,
        hash,
    };

    SvgRangedFrame { range, render }
}

/// Result of SVG rendering, containing SVG frames and diagnostics.
// #[boltffi::data]
pub struct SvgRender {
    /// Rendered SVG frames for each chunk.
    pub frames: Vec<SvgRangedFrame>,
    /// Rendered SVG frames for tooltips.
    pub tooltips: Vec<SvgRangedFrame>,
    /// Diagnostics and warnings produced during rendering.
    pub diagnostics: Vec<EditorDiagnostic>,
}

/// An SVG frame with its corresponding source range.
#[derive(Clone)]
#[boltffi::data]
pub struct SvgRangedFrame {
    /// UTF-16 range in the source for this frame.
    pub range: Range<usize>,
    /// The SVG render data.
    pub render: SvgFrameRender,
}

impl SvgRangedFrame {
    #[must_use]
    pub const fn new(range: Range<usize>, render: SvgFrameRender) -> Self {
        Self { range, render }
    }
}

/// Rendered SVG data for a frame, including metadata.
#[derive(Clone)]
#[boltffi::data]
pub struct SvgFrameRender {
    /// SVG markup as a string.
    pub svg: String,
    /// Width of the frame in points.
    pub width: f64,
    /// Height of the frame in points.
    pub height: f64,
    /// Offset from the left of the page in points.
    pub x_offset: f64,
    /// Hash of the frame items for change detection.
    pub y_offset: f64,
    /// Hash of the frame items for change detection.
    pub hash: u32,
}
