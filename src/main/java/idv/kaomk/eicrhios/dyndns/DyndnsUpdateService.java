package idv.kaomk.eicrhios.dyndns;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DyndnsUpdateService implements Callable<String>, Runnable {
	private Logger logger = LoggerFactory.getLogger(DyndnsUpdateService.class);

	private long mPeriod;

	private TimeUnit mTimeUnit;

	private URL mDnsUpdateUrl;

	private String mUsername;

	private String mPassword;

	private String mHostname;

	private String mStatus;

	private ScheduledFuture<?> mUpdateDnsFuture;

	@Override
	public void run() {
		try {
			call();
		} catch (IOException e) {
			logger.info("update dns failed:", e);
		} catch (Exception e) {
			logger.error("unexpectd exception:", e);
			mUpdateDnsFuture.cancel(true);
			mUpdateDnsFuture = null;
		}
	}

	@Override
	public String call() throws IOException {
		BufferedReader reader = null;
		try {
			logger.debug(String.format("try to connect to mDnsUpdateUrl: %s",
					mDnsUpdateUrl.toString()));

			HttpURLConnection conn = (HttpURLConnection) mDnsUpdateUrl
					.openConnection();

			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "text/html;charset=UTF-8");

			conn.connect();

			int responseCode = conn.getResponseCode();
			logger.debug(String.format("mDnsUpdateUrl response code: %d",
					responseCode));
			reader = new BufferedReader(new InputStreamReader(
					responseCode == HttpURLConnection.HTTP_OK ? conn
							.getInputStream() : conn.getErrorStream()));

			if (responseCode == HttpURLConnection.HTTP_OK) {
				mStatus = reader.readLine();
				logger.debug(String.format("mStatus: %s", mStatus));
				return mStatus;
			} else {
				String line;
				StringBuilder sb = new StringBuilder();
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				throw new IOException(sb.toString());
			}
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
				}
		}

	}

	private ScheduledExecutorService mExecutor = Executors
			.newSingleThreadScheduledExecutor();

	public void start() throws MalformedURLException {
		if (logger.isDebugEnabled()) {
			logger.debug("DyndnsUpdateService.start");
			logger.debug(String.format(
					"scheduleAtFixedRate param: period=%1$s, timeUnit=%2$s",
					mPeriod, mTimeUnit.toString()));

		}

		mDnsUpdateUrl = new URL(String.format(
				"http://%1$s:%2$s@members.dyndns.org/nic/update?hostname=%3$s",
				mUsername, mPassword, mHostname));

		mUpdateDnsFuture = mExecutor.scheduleAtFixedRate(this, 0, mPeriod,
				mTimeUnit);

	}

	public void stop() {
		logger.debug("DyndnsUpdateService.stop");
		mExecutor.shutdownNow();
	}

	public String refresh() throws InterruptedException, ExecutionException {
		ScheduledFuture<String> future = mExecutor.schedule(
				(Callable<String>) this, 0, TimeUnit.SECONDS);
		return future.get();
	}

	public long getPeriod() {
		return mPeriod;
	}

	public void setPeriod(long period) {
		mPeriod = period;
	}

	public String getTimeUnit() {
		return mTimeUnit.toString();
	}

	public void setTimeUnit(String timeUnit) {
		mTimeUnit = TimeUnit.valueOf(TimeUnit.class, timeUnit);
	}

	public String getUsername() {
		return mUsername;
	}

	public void setUsername(String username) {
		mUsername = username;
	}

	public String getPassword() {
		return mPassword;
	}

	public void setPassword(String password) {
		mPassword = password;
	}

	public String getHostname() {
		return mHostname;
	}

	public void setHostname(String hostname) {
		mHostname = hostname;
	}

	public URL getDnsUpdateUrl() {
		return mDnsUpdateUrl;
	}

	public String getStatus() {
		return mStatus;
	}

}
