package webstart.browser;

import org.apache.log4j.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.CloseWindowListener;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.browser.VisibilityWindowListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;


public class MiniBrowserWindow 
{
	final static Logger __log = Logger.getLogger( MiniBrowserWindow.class );

	static int __counter = 0; 
	
	Display   _display;
	Shell     _shell;
	Browser   _browser;
	
	public Shell getShell() { return _shell; }
	public Browser getBrowser() { return _browser; }
	
	int _id;  // for debugging; running instance/object number using __counter (e.g. 1/2/3/4 etc.)
	
	String _title;
	Image  _images[];
	
	
	final static String UNTITLED = "Untitled Mini Browser"; 
	
	//////////
	// public top-level ctors

	public MiniBrowserWindow( Display display )
	{
		this( display, UNTITLED, null );
	}

	public MiniBrowserWindow( Display display, String title, Image images[] )
	{
		__log.debug( "ctor" );
		init( display, title, images );

		onCreateMainWindow();
	}		
	
	/////////////////////////
	// protected popup window (sub shells) ctors (called for popups)
	
	protected MiniBrowserWindow( MiniBrowserWindow parent ) 
	{
	   __log.debug( "ctor w/ parent" );
		
		// NB: for sub shells inherit (pass along)  display, title, images, etc.
	   init( parent._display, parent._title, parent._images );

	   onCreatePopUpWindow();
	}

	public void onCreateMainWindow()   {  /* do nothing; hook */ }	
    public void onCreatePopUpWindow()   {  /* do nothing; hook */ }	

    public MiniBrowserWindow createPopUpWindow()
    {
  	    __log.debug( "#"+_id+"| createPopUpWindow called" );
    	return new MiniBrowserWindow( this );
    }    
    
    
    
    
	private void init( Display display, String title, Image images[] )
	{
		__counter++;
		_id = __counter;
		
		_display     = display;		
		_title       = title;
		_images      = images;
		
		_shell = new Shell( _display );
		_shell.setLayout( new FillLayout() );
		
		_shell.setText( _title );
		if( _images != null )
			_shell.setImages( _images );		

		createBrowser();	
	}
	
	private void createBrowser()
	{
		__log.debug( "#"+_id+"| before create browser widget" );
		
		_browser = new Browser( _shell, SWT.NONE );
		
		__log.debug( "#"+_id+"| after create browser widget" );

		// title handler gets title from web page (lets us update shell/window title using web page title)
		_browser.addTitleListener( new TitleListener() 
		{
			public void changed( TitleEvent ev )
			{
				__log.debug( "#"+_id+"| browser-title-changed: title=" +ev.title );
				
				if( ev.title.startsWith( "http://" ) || ev.title.contains( "application/pdf" ))
				   return;
				
				_shell.setText( ev.title );
			}
		});

		
	 _browser.addOpenWindowListener( new OpenWindowListener() {
		public void open( WindowEvent event ) {
			__log.debug( "#"+_id+"| browser-window-open: required=" + event.required );

			// if (!event.required) return;	/* only do it if necessary */
			// NB: always create our own window/shell

			MiniBrowserWindow win =  MiniBrowserWindow.this.createPopUpWindow();
			win.getShell().open();
			event.browser = win.getBrowser();		
		}
	});

	 _browser.addVisibilityWindowListener( new VisibilityWindowListener() {
		public void hide( WindowEvent event ) {
			__log.debug( "#"+_id+"| browser-window-hide" );
			Browser browser = (Browser) event.widget;
			Shell shell = browser.getShell();
			shell.setVisible( false );
		}
		
		public void show( WindowEvent ev ) {
			__log.debug( "#"+_id+"| browser-window-show" );			
			Browser browser = (Browser) ev.widget;
			Shell shell = browser.getShell();
			
			if( ev.location != null ) {
				__log.debug( "#"+_id+"| location.x=" + ev.location.x + ", y=" + ev.location.y );
				shell.setLocation( ev.location );
			}

			if( ev.size != null ) {
				__log.debug( "#"+_id+"| size.x=" + ev.size.x + ", y=" + ev.size.y );
				Point size = ev.size;
				shell.setSize( shell.computeSize( size.x, size.y ) );
			}
			shell.open();
		}
	});
	 
	_browser.addCloseWindowListener( new CloseWindowListener() {
		// NB: only fired/called if user clicks exit symbol in web page 
		public void close( WindowEvent event ) {
			__log.debug( "#"+_id+"| browser-window-close" );

			Browser browser = (Browser) event.widget;
			Shell shell = browser.getShell();
			shell.close();
		}
	});
	} // method createBrowser
	
} // class MiniBrowserWindow
