mod imp;

use glib::Object;
use gtk::glib;
use gtk::subclass::prelude::*;

glib::wrapper! {
    pub struct ActivityBarItem(ObjectSubclass<imp::ActivityBarItem>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl ActivityBarItem {
    pub fn new(icon_name: &str, _tooltip: &Option<String>, action_name: &Option<String>) -> Self {
        let item: ActivityBarItem = Object::builder().property("icon-name", icon_name).build();
        if let Some(action_name) = action_name.as_deref() {
            item.imp().bind_action(action_name);
        }
        item
    }
}

#[repr(u8)]
#[derive(Default, Debug, Clone, Copy)]
pub enum ItemStatus {
    #[default]
    Off = 0,
    On 
}

impl ItemStatus {
    pub fn to_u8(self) -> u8 {
        self as u8
    }

    pub fn from_u8(u: u8) -> ItemStatus {
        match u {
            0 => Self::Off,
            1 => Self::On,
            _ => unimplemented!()
        }
    }
}
