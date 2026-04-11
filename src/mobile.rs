use serde::Serialize;
use tauri::{
    plugin::{PluginApi, PluginHandle},
    AppHandle, Runtime,
};

pub fn init<R: Runtime, C: serde::de::DeserializeOwned>(
    _app: &AppHandle<R>,
    api: PluginApi<R, C>,
) -> tauri::Result<AndroidWidgets<R>> {
    let handle = api.register_android_plugin(
        "moe.astralsight.astrobox.plugin.android_widgets",
        "AndroidWidgetsPlugin",
    )?;
    Ok(AndroidWidgets(handle))
}

pub struct AndroidWidgets<R: Runtime>(PluginHandle<R>);

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct SyncWidgetSnapshotRequest {
    pub json: String,
}

impl<R: Runtime> AndroidWidgets<R> {
    pub fn sync_widget_snapshot(&self, payload: SyncWidgetSnapshotRequest) -> tauri::Result<()> {
        self.0
            .run_mobile_plugin("syncWidgetSnapshot", payload)
            .map_err(Into::into)
    }
}
