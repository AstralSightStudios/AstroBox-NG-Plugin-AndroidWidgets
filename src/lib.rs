use tauri::{
    plugin::{Builder, TauriPlugin},
    Manager, Runtime,
};

#[cfg(target_os = "android")]
mod mobile;

#[cfg(target_os = "android")]
pub use mobile::SyncWidgetSnapshotRequest;

#[cfg(target_os = "android")]
use mobile::AndroidWidgets;

pub trait AndroidWidgetsExt<R: Runtime> {
    #[cfg(target_os = "android")]
    fn android_widgets(&self) -> &AndroidWidgets<R>;
}

impl<R: Runtime, T: Manager<R>> AndroidWidgetsExt<R> for T {
    #[cfg(target_os = "android")]
    fn android_widgets(&self) -> &AndroidWidgets<R> {
        self.state::<AndroidWidgets<R>>().inner()
    }
}

pub fn init<R: Runtime>() -> TauriPlugin<R> {
    Builder::new("android-widgets")
        .setup(|_app, _api| {
            #[cfg(target_os = "android")]
            {
                let widgets = mobile::init(_app, _api)?;
                _app.manage(widgets);
            }

            Ok(())
        })
        .build()
}
