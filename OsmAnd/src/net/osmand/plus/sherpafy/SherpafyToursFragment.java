package net.osmand.plus.sherpafy;

import java.util.List;

import net.osmand.IProgress;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.activities.DownloadIndexActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public class SherpafyToursFragment extends SherlockListFragment {
	private static final int ACTION_DOWNLOAD = 5;
	OsmandApplication app;
	private View view;
	private SherpafyCustomization custom;

	public SherpafyToursFragment(OsmandApplication app) {
		this.app = app;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		OsmandApplication app = (OsmandApplication) getSherlockActivity().getApplication();
		custom = (SherpafyCustomization) app.getAppCustomization();
		TourAdapter tourAdapter = new TourAdapter(custom.getTourInformations());
		setListAdapter(tourAdapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		OsmandApplication app = (OsmandApplication) getActivity().getApplication();
		boolean light = true; //app.getSettings().isLightActionBar();
		com.actionbarsherlock.view.MenuItem menuItem = menu.add(0, ACTION_DOWNLOAD, 0, R.string.download_tours).setShowAsActionFlags(
				MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		//menuItem = menuItem.setIcon(light ? R.drawable.ic_action_gdirections_light : R.drawable.ic_action_gdirections_dark);
		menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
				if (custom.getAccessCode().length() == 0) {
					openAccessCode(true);
				} else {
					startDownloadActivity();
				}
				return true;
			}
		});

	}
	
	protected void openAccessCode(final boolean startDownload) {
		Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.enter_access_code);
		final EditText editText = new EditText(getActivity());
		editText.setInputType(EditorInfo.TYPE_TEXT_FLAG_CAP_CHARACTERS);
		LinearLayout ll = new LinearLayout(getActivity());
		ll.setPadding(5, 3, 5, 0);
		ll.addView(editText, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		builder.setView(ll);
		builder.setNegativeButton(R.string.default_buttons_cancel, null);
		builder.setPositiveButton(R.string.default_buttons_yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String acCode = editText.getText().toString();
				if (!custom.setAccessCode(acCode)) {
					Toast.makeText(getActivity(), R.string.access_code_is_not_valid, Toast.LENGTH_LONG).show();
					return;
				}
				if (startDownload) {
					startDownloadActivity();
				}
			}
		});
		builder.create().show();
	}
	
	private void startDownloadActivity() {
		final Intent download = new Intent(getActivity(), DownloadIndexActivity.class);
		download.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(download);
	}
	
	private void selectTourAsync(final TourInformation tour) {
		new AsyncTask<TourInformation, Void, Void>() {
			private ProgressDialog dlg;

			protected void onPreExecute() {
				dlg = new ProgressDialog(getActivity());
				dlg.setTitle(R.string.selecting_tour_progress);
				dlg.setMessage(getString(R.string.indexing_tour, tour == null ? "" : tour.getName()));
				dlg.show();
			};

			@Override
			protected Void doInBackground(TourInformation... params) {
				// if tour is already selected - do nothing
				if (custom.getSelectedTour() != null) {
					if (custom.getSelectedTour().equals(params[0])) {
						return null;
					}
				}
				custom.selectTour(params[0], IProgress.EMPTY_PROGRESS);
				return null;
			}

			protected void onPostExecute(Void result) {
				// to avoid illegal argument exception when rotating phone during loading
				try {
					dlg.dismiss();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				//startTourView();
			};
		}.execute(tour);
	}
	
	class TourAdapter extends ArrayAdapter<TourInformation> {

		public TourAdapter(List<TourInformation> list) {
			super(getActivity(), R.layout.search_history_list_item, list);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = getActivity().getLayoutInflater();
				row = inflater.inflate(R.layout.sherpafy_list_tour_item, parent, false);
			}
			TourInformation ti = getItem(position);
			TextView description = (TextView) row.findViewById(R.id.TourDescription);
			TextView name = (TextView) row.findViewById(R.id.TourName);
			description.setText(ti.getShortDescription());
			name.setText(ti.getName());
			ImageView iv = (ImageView) row.findViewById(R.id.TourImage);
			if(ti.getImageBitmap() != null) {
				iv.setBackground(new BitmapDrawable(getResources(), ti.getImageBitmap()));
			}
			return row;
		}
	}

}