package idv.kaomk.eicrhios.dyndns;

import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.apache.felix.gogo.commands.*;

@Command(scope = "dyndns", name = "refresh", description = "Perform an dns update right now")
public class RefreshAction extends OsgiCommandSupport {
	private DyndnsUpdateService mDyndnsUpdateService;

	@Override
	protected Object doExecute() throws Exception {
		return String.format("status: %s", mDyndnsUpdateService.refresh());
	}

	public DyndnsUpdateService getDyndnsUpdateService() {
		return mDyndnsUpdateService;
	}

	public void setDyndnsUpdateService(DyndnsUpdateService dyndnsUpdateService) {
		mDyndnsUpdateService = dyndnsUpdateService;
	}

}
