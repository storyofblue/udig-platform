package net.refractions.udig.catalog.internal.wms.ui;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.internal.wms.WMSServiceExtension;
import net.refractions.udig.catalog.internal.wms.WMSServiceImpl;
import net.refractions.udig.catalog.internal.wms.WmsPlugin;
import net.refractions.udig.catalog.ui.AbstractUDIGImportPage;
import net.refractions.udig.catalog.ui.UDIGConnectionPage;
import net.refractions.udig.catalog.ui.workflow.EndConnectionState;
import net.refractions.udig.catalog.wms.internal.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

/**
 * Data page responsible for aquiring WMS services.
 * <p>
 * Responsibilities:
 * <ul>
 * <li>defaults based on selection - for URL, WMSService, and generic IService (from search)
 * <li>remember history in dialog settings
 * <li>complete list here: <a
 * href="http://udig.refractions.net/confluence/display/DEV/UDIGImportPage+Checklist">Import Page
 * Checklist</a>
 * </ul>
 * </p>
 * <p>
 * This page is used in the Import and Add Layer wizards.
 * </p>
 * 
 * @author jgarnett
 * @since 1.0.0
 */
public class WMSWizardPage extends AbstractUDIGImportPage implements ModifyListener, UDIGConnectionPage {

    final static String[] types = {"WMS", "Directory"}; //$NON-NLS-1$ //$NON-NLS-2$

    private String url = ""; //$NON-NLS-1$

    private static final String WMS_WIZARD = "WMS_WIZARD"; //$NON-NLS-1$
    private static final String WMS_RECENT = "WMS_RECENT"; //$NON-NLS-1$
    private IDialogSettings settings;
    private static final int COMBO_HISTORY_LENGTH = 15;

    private Combo urlCombo;

    /**
     * Construct <code>WMSWizardPage</code>.
     * 
     * @param pageName
     */
    public WMSWizardPage() {
        super(Messages.WMSWizardPage_title); 

        settings = WmsPlugin.getDefault().getDialogSettings().getSection(WMS_WIZARD);
        if (settings == null) {
            settings = WmsPlugin.getDefault().getDialogSettings().addNewSection(WMS_WIZARD);
        }
    }

    public String getId() {
        return "net.refractions.udig.catalog.ui.WMS"; //$NON-NLS-1$
    }

    /** Can be called during createControl */
    protected Map<String,Serializable> defaultParams(){
    	IStructuredSelection selection = (IStructuredSelection)PlatformUI
			.getWorkbench() .getActiveWorkbenchWindow().getSelectionService()
			.getSelection();
        Map<String, Serializable> toParams = toParams( selection );
        if( !toParams.isEmpty() ){
        	return toParams;
        }
        
    	WMSConnectionFactory connectionFactory = new WMSConnectionFactory();
    	Map<String, Serializable> params = connectionFactory.createConnectionParameters( getState().getWorkflow().getContext() );
    	if( params !=null )
    		return params;
    	
    	return Collections.emptyMap();
    }
    /** Retrieve "best" WMS guess of parameters based on provided context */
    protected Map<String,Serializable> toParams( IStructuredSelection context){
        if( context != null ) {
        	WMSConnectionFactory connectionFactory = new WMSConnectionFactory();
            for( Iterator itr = context.iterator(); itr.hasNext(); ) {
				Map<String,Serializable> params = connectionFactory
                	.createConnectionParameters(itr.next());
                if( !params.isEmpty() ) return params;
            }
        }
        return Collections.EMPTY_MAP;
    }
  
    public void createControl( Composite parent ) {
        String[] recentWMSs = settings.getArray(WMS_RECENT);
        if (recentWMSs == null) {
            recentWMSs = new String[0];
        }

        GridData gridData;
        Composite composite = new Composite(parent, SWT.NULL);

        GridLayout gridLayout = new GridLayout();
        int columns = 1;
        gridLayout.numColumns = columns;
        composite.setLayout(gridLayout);

        gridData = new GridData();

        Label urlLabel = new Label(composite, SWT.NONE);
        urlLabel.setText(Messages.WMSWizardPage_label_url_text); 
        urlLabel.setLayoutData(gridData);

        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.widthHint = 400;

        // For Drag 'n Drop as well as for general selections
        // look for a url as part of the selection
        Map<String,Serializable> params = defaultParams(); // based on selection
        
        urlCombo = new Combo(composite, SWT.BORDER);
        urlCombo.setItems(recentWMSs);
        urlCombo.setVisibleItemCount(15);
        urlCombo.setLayoutData(gridData);
        
        URL selectedURL = getURL( params );
        if (selectedURL != null) {
            urlCombo.setText( selectedURL.toExternalForm() );
            url = selectedURL.toExternalForm();
            setPageComplete(true);
        } else if (url != null && url.length() != 0) {
            urlCombo.setText(url);
            setPageComplete(true);
        } else {
        	url = null;
            urlCombo.setText("http://"); //$NON-NLS-1$
            setPageComplete(false);
        }
        urlCombo.addModifyListener(this);

        setControl(composite);
        
        Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				
				EndConnectionState currentState = getState();
				Map<IService, Throwable> errors = currentState.getErrors();
				if( errors!=null && !errors.isEmpty()){
					for (Map.Entry<IService, Throwable> entry : errors.entrySet()) {
						if( entry.getKey() instanceof WMSServiceImpl ){
							Throwable value = entry.getValue();
							if( value instanceof ConnectException){
								setErrorMessage(Messages.WMSWizardPage_serverConnectionError);
							}else{
								String message = Messages.WMSWizardPage_connectionProblem+value.getLocalizedMessage();
								setErrorMessage(message);
							}
						}
					}
				}

			}
		});
    }

    @Override
    public void setErrorMessage(String newMessage) {
    	WizardPage page=(WizardPage) getContainer().getCurrentPage();
    	page.setErrorMessage(newMessage);
    }

    @Override
    public void setMessage(String newMessage) {
    	WizardPage page=(WizardPage) getContainer().getCurrentPage();
    	page.setMessage(newMessage);
    }
    
    @Override
    public void setMessage(String newMessage, int messageType) {
    	WizardPage page=(WizardPage) getContainer().getCurrentPage();
    	page.setMessage(newMessage, messageType);
    }

	public EndConnectionState getState() {
		return (EndConnectionState) super.getState();
	}
    
    public URL getURL( Map<String,Serializable> params ){
        Object value = params.get( WMSServiceImpl.WMS_URL_KEY );
        if( value == null ) return null;
        if( value instanceof URL ) return (URL) value;
        if( value instanceof String) {
            try {
                URL url = new URL( (String) value );   
                return url;
            }
            catch( MalformedURLException erp ){                
            }
        }
        return null;        
    }
    /**
     * Double click in list, or return from url control.
     * 
     * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
     * @param e
     */
    public void widgetDefaultSelected( SelectionEvent e ) {
        e.getClass();// kill warning
        if (getWizard().canFinish()) {
            getWizard().performFinish();
        }
    }

    
    
    /**
     * This should be called using the Wizard .. job when next/finish is pressed.
     */
    public List<IService> getResources( IProgressMonitor monitor ) throws Exception {
        URL location = new URL(url);
        
        WMSServiceExtension creator = new WMSServiceExtension();

        Map<String, Serializable> params = creator.createParams(location);
        IService service = creator.createService(location, params);
        service.getInfo(monitor); // load it

        List<IService> servers = new ArrayList<IService>();
        servers.add(service);

        /*
         * Success! Store the URL in history.
         */
        saveWidgetValues();

        return servers;
    }

    public void modifyText( ModifyEvent e ) {
        try {
        	getState().getErrors().clear();
            url = ((Combo) e.getSource()).getText();
            new URL(url);
            setErrorMessage(null);
            setPageComplete(true);
        } catch (MalformedURLException exception) {
            setErrorMessage(Messages.WMSWizardPage_error_invalidURL); 
            setPageComplete(false);
        }
        
        getWizard().getContainer().updateButtons();
    }

    /**
     * Saves the widget values
     */
    private void saveWidgetValues() {
        // Update history
        if (settings != null) {
            String[] recentWMSs = settings.getArray(WMS_RECENT);
            if (recentWMSs == null) {
                recentWMSs = new String[0];
            }
            recentWMSs = addToHistory(recentWMSs, url);
            settings.put(WMS_RECENT, recentWMSs);
        }
    }

    /**
     * Adds an entry to a history, while taking care of duplicate history items and excessively long
     * histories. The assumption is made that all histories should be of length
     * <code>COMBO_HISTORY_LENGTH</code>.
     * 
     * @param history the current history
     * @param newEntry the entry to add to the history
     * @return the history with the new entry appended Stolen from
     *         org.eclipse.team.internal.ccvs.ui.wizards.ConfigurationWizardMainPage
     */
    private String[] addToHistory( String[] history, String newEntry ) {
        ArrayList<String> l = new ArrayList<String>(Arrays.asList(history));
        addToHistory(l, newEntry);
        String[] r = new String[l.size()];
        l.toArray(r);
        return r;
    }

    /**
     * Adds an entry to a history, while taking care of duplicate history items and excessively long
     * histories. The assumption is made that all histories should be of length
     * <code>COMBO_HISTORY_LENGTH</code>.
     * 
     * @param history the current history
     * @param newEntry the entry to add to the history Stolen from
     *        org.eclipse.team.internal.ccvs.ui.wizards.ConfigurationWizardMainPage
     */
    private void addToHistory( List<String> history, String newEntry ) {
        history.remove(newEntry);
        history.add(0, newEntry);

        // since only one new item was added, we can be over the limit
        // by at most one item
        if (history.size() > COMBO_HISTORY_LENGTH)
            history.remove(COMBO_HISTORY_LENGTH);
    }

    public Map<String, Serializable> getParams() {
        try {
            URL location = new URL(url);

            WMSServiceExtension creator = new WMSServiceExtension();
            String errorMessage = creator.reasonForFailure(location);
            if (errorMessage != null) {
                setErrorMessage(errorMessage);
                return Collections.emptyMap();
            } else
                return creator.createParams(location);
        } catch (MalformedURLException e) {
            return null;
        }
    }

	public List<URL> getURLs() {
		try {
			ArrayList<URL> l = new ArrayList<URL>();
			l.add(new URL(url));
			
			return l;
		}
		catch(MalformedURLException e) {
			return null;
		}
	}
    
}

