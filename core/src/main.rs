use std::{path::PathBuf, thread, time::Duration};

use loro::LoroDoc;
use mnemo_core::storage::{fs::init_fs, workspace::spaces::Spaces};

fn main() {
    init_fs(PathBuf::from("data")).expect("Failed to initialize FS");

    let doc = LoroDoc::new();

    let container = doc.get_map("spaces.json");
    let spaces = Spaces::new(container);
    spaces.subscribe();

    let space = spaces
        .container
        .get("029nhv7546")
        .expect("Failed to get space")
        .into_value()
        .expect("Failed to convert to value");
    spaces
        .container
        .insert("test", space)
        .expect("Failed to insert test space");

    dbg!(spaces.container.get_value());

    // dbg!(spaces);

    doc.commit();
}
