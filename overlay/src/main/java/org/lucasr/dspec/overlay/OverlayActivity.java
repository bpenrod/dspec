package org.lucasr.dspec.overlay;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

public class OverlayActivity extends AppCompatActivity {
	private static final int REQUEST_OVERLAY_PERMISSION = 0x100;
	private ArrayList<String> assetFiles;
	private String[] assetFileNames;
	private int selectedAsset = 0;
	private Switch showHideSwitch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.overlay_activity);

		assetFiles = collectAssetFiles();
		assetFileNames = new String[assetFiles.size()];
		int i = 0;
		for (String file : assetFiles) {
			assetFileNames[i++] = file;
		}

		showHideSwitch = (Switch) findViewById(R.id.showHideSwitch);
		showHideSwitch.setOnCheckedChangeListener(onOffSwitchListener);

		updateSelectedAsset();

		findViewById(R.id.selected_asset).setClickable(true);
		findViewById(R.id.selected_asset).setOnClickListener(assetSelectListener);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_OVERLAY_PERMISSION) {
			//check whether permission was granted
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (!Settings.canDrawOverlays(this)) {
					// ACTION_MANAGE_OVERLAY_PERMISSION permission not granted...
					showHideSwitch.setChecked(false);
				} else {
					// ACTION_MANAGE_OVERLAY_PERMISSION permission granted
					showHideSwitch.setChecked(true);
				}
			}
		}
	}

	private OnCheckedChangeListener onOffSwitchListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			final Intent intent = new Intent(OverlayActivity.this, DisplayOverlayService.class);

			if (isChecked) {
				if (checkManageOverlayPermission()) {
					return;
				}
				if (selectedAsset < assetFiles.size()) {
					String fileName = assetFiles.get(selectedAsset);
					intent.putExtra(DisplayOverlayService.ASSET_FILE, fileName);
					startService(intent);
				}
			} else {
				stopService(intent);
			}
		}
	};

	private View.OnClickListener assetSelectListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			AlertDialog.Builder builder = new AlertDialog.Builder(OverlayActivity.this);
			builder.setTitle(R.string.title_select_asset);
			builder.setItems(assetFileNames, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which != selectedAsset) {
						selectedAsset = which;
						updateSelectedAsset();
					}
					dialog.dismiss();
				}
			});
			builder.show();
		}
	};

	private boolean checkManageOverlayPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (!Settings.canDrawOverlays(this)) {
				Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
												  Uri.parse("package:" + getPackageName()));
				startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
				return true;
			}
		}
		return false;
	}

	private void updateSelectedAsset() {
		TextView assetDesc = (TextView) findViewById(R.id.selected_asset_summary);
		if (selectedAsset < assetFiles.size()) {
			String fileName = assetFiles.get(selectedAsset);
			assetDesc.setText(getString(R.string.description_selected_asset,
					fileName.replaceFirst("^(.*)\\.json$","$1")));
			if (showHideSwitch.isChecked()) {
				final Intent intent = new Intent(OverlayActivity.this, DisplayOverlayService.class);
				intent.putExtra(DisplayOverlayService.ASSET_FILE, fileName);
				startService(intent);
			}
		} else {
			assetDesc.setText(getText(R.string.description_selected_asset_none));
		}
	}

	@NonNull
	private ArrayList<String> collectAssetFiles() {
		ArrayList<String> specFileNames = new ArrayList<>();
		try {
			for (String fileName : getAssets().list("")) {
				if (fileName.endsWith(".json")) {
					specFileNames.add(fileName);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return specFileNames;
	}
}
