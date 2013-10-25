package webstart;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;

import webstart.browser.MiniBrowserWindow;
import webstart.browser.MiniBrowserWindowEx;


public class TestMiniBrowser 
{
	final static Logger __log = Logger.getLogger( TestMiniBrowser.class );

	public static void main( String args[] )
	{
		BasicConfigurator.configure();
		__log.debug( "log4j configured; lets go" );
		
		String url = (args.length == 0) ?  "http://google.com" : args[0];
				
		Display display = new Display();
		
		String refreshPath[] = {};

		MiniBrowserWindow win = new MiniBrowserWindowEx( display, refreshPath );
		// MiniBrowserWindow win = new MiniBrowserWindow( display );
		
		win.getBrowser().setUrl( url );
		win.getShell().open();
		
		while( !win.getShell().isDisposed() ) 
		{
		   if( !display.readAndDispatch() )
				display.sleep();
		}
		display.dispose();
		
		__log.info( "bye" );
	}	
}  // class TestMiniBrowser
