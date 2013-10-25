package webstart.browser;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;


public class MiniBrowserWindowEx extends MiniBrowserWindow 
{
	final static Logger __log = Logger.getLogger( MiniBrowserWindowEx.class );
	
    //////////
	// standard ctors and createSubShell machinery 

	String _refreshPaths[];
	
	public MiniBrowserWindowEx( Display display, String refreshPaths[] )
	{
	   this( display, UNTITLED, null, refreshPaths );
	}
	
    public MiniBrowserWindowEx( Display display, String title, Image images[], String refreshPaths[] )
	{
	   super( display, title, images );
	   
	   _refreshPaths = refreshPaths;
	}

    
	protected MiniBrowserWindowEx( MiniBrowserWindowEx parent ) 
	{
	   super( parent );
	   
	   _refreshPaths = parent._refreshPaths;
	}

	@Override
	public MiniBrowserWindow createPopUpWindow()
	{
	  __log.debug( "#"+_id+"| createPopUpWindow called" );
	  return new MiniBrowserWindowEx( this );
	}

	
	///////////////////////////////////////////
	// add extra variables and code 

	static int     __openWindowCounter = 0; // track open pop-ups (NB: will NOT include top level window)	
    static boolean __canClose = false;

    boolean _firstPage = true;   // HACK: refresh first page
    
    
	static class OpenWindowCountFun extends BrowserFunction {
	    private MiniBrowserWindowEx _win;
		OpenWindowCountFun( MiniBrowserWindowEx win, String name ) {
			super( win.getBrowser(), name );
			_win = win;
		}
			
		public Object function(Object[] arguments) {
            __log.debug( "OpenWindowCountFun.function called" );
			return new Short( (short) _win.__openWindowCounter );
		}
	}

	static class SetCanCloseFlagFun extends BrowserFunction {
		private MiniBrowserWindowEx _win;
		SetCanCloseFlagFun( MiniBrowserWindowEx win, String name ) {
			super( win.getBrowser(), name );
			_win = win;
		}
			
		public Object function(Object[] arguments) {
            __log.debug( "SetCanCloseFlagFun.function called" );
			_win.__canClose = true;
			return new Boolean( _win.__canClose );
		}
	}
	  
	static class HelloFun extends BrowserFunction {
		HelloFun( MiniBrowserWindowEx win, String name ) {
			super( win.getBrowser(), name );
		}
		
		public Object function (Object[] arguments) {
            __log.debug( "HelloFun.function called" );
			return new String( "Hello from Webrunner!" );
		}
	}

	@Override
	public void onCreateMainWindow()
	{
	  __log.debug( "#"+_id+"| create main window (top browser shell)" );
	   
	  __log.debug( "#"+_id+"| adding browser functions" );
		
	    final BrowserFunction fun1 = new HelloFun( this, "webrunnerHello" );
		final BrowserFunction fun2 = new OpenWindowCountFun( this, "webrunnerOpenWindowCount" );
		final BrowserFunction fun3 = new SetCanCloseFlagFun( this, "webrunnerSetCanCloseFlag" );
		
		_shell.addListener( SWT.Close, new Listener() {
			    public void handleEvent( Event ev ) {			        
			      __log.debug( "#"+_id+"| shell-close: openWindowCounter: " + __openWindowCounter + ", canClose: " + __canClose );
			    	  
			      if( __openWindowCounter > 0 && __canClose == false )
			      {
			    	ev.doit = false;

					String js = "if( typeof webrunner !== 'undefined' && typeof webrunner.closeWindow === 'function') { webrunner.closeWindow(); } else { webrunnerSetCanCloseFlag(); window.close(); }";
						 
					__log.debug( "#"+_id+"| browser-execute: >" +js + "<" );
					_browser.execute( js );
			      }
			    } // method handleEvent	
			});
	}

	@Override
	public void onCreatePopUpWindow()
	{
		__log.debug( "#"+_id+"| create popup window (sub browser shell)" );

		_shell.addListener( SWT.Close, new Listener() {
		      public void handleEvent( Event event ) {
		    	  __openWindowCounter--;
		    	  __log.debug( "#"+_id+"| shell-close: openWindowCounter--: " + __openWindowCounter );
		    	  
		    	  // __log.debug( "#"+_id+"| shell-close: clearSessions" );
		    	  // Browser.clearSessions();
		      }	
		});
		
		_browser.addProgressListener( new ProgressListener() {
		      public void changed( ProgressEvent ev ) {
		          if(ev.total == 0) 
		        	  return;                            
		          int ratio = ev.current * 100 / ev.total;
		  		  __log.debug( "#"+_id+"| browser-progress-changed - current: " + ev.current + ", total: " + ev.total );
		      }
		      public void completed( ProgressEvent ev ) {
		  		  __log.debug( "#"+_id+"| browser-progress-completed - firstPage: " + _firstPage + ", url: " + _browser.getUrl() );

		  		  if( _firstPage == true )
		  		  {
		  			  for( String path : _refreshPaths )
		  			  {
		  				if( _browser.getUrl().contains( path ))			  			
					  	{
						  __log.debug( "#"+_id+"| refresh first page" );
					  	  _browser.refresh();
					  	  break;
					     }  
		  			  }
			  		  
		  		     _firstPage = false;
		  		  }
		      }
		    });

		// track open popups count
		__openWindowCounter++;
		__log.debug( "#"+_id+"| shell-open: openWindowCounter++: " + __openWindowCounter );
	}
	
} // class MiniBrowserWindowEx
