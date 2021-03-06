package com.athaydes.automaton.samples

import com.athaydes.automaton.Swinger
import com.athaydes.automaton.SwingerFxer
import com.athaydes.automaton.samples.apps.SwingWithFXSample
import com.athaydes.automaton.selector.SimpleSwingerSelector
import javafx.embed.swing.JFXPanel
import javafx.scene.paint.Color
import javafx.scene.paint.LinearGradient
import javafx.scene.text.Text
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import javax.swing.*
import java.awt.*
import java.awt.event.KeyEvent
import java.util.concurrent.ArrayBlockingQueue

import static com.athaydes.automaton.assertion.AutomatonMatcher.hasText
import static com.athaydes.automaton.assertion.AutomatonMatcher.hasValue
import static java.util.concurrent.TimeUnit.SECONDS
import static org.junit.Assert.assertThat

/**
 *
 * User: Renato
 */
class SwingJavaFXSampleAppTest {

	static JFrame jFrame
	static JFXPanel jfxPanel

	@BeforeClass
	static void setup( ) {
		def blockUntilReady = new ArrayBlockingQueue( 1 )
		def app = new SwingWithFXSample()
		app.createAndRunSwingApp( blockUntilReady )
		assert blockUntilReady.poll( 5, SECONDS )
		jFrame = app.jFrame
		jfxPanel = app.jfxPanel
		println "Gui ready!"
	}

	@AfterClass
	static void cleanup( ) {
		jFrame?.dispose()
	}

	@Test
	void "Automaton should be able to test applications using both Swing and JavaFX"( ) {
		final swingTextAreaText = "Hello, I am Swing..."
		final fxInputText = "Hello, JavaFX..."

		def swfx = SwingerFxer.getUserWith( jFrame, jfxPanel.scene.root )

		swfx.doubleClickOn( "text:colors" )
				.clickOn( "text-area" )
				.type( swingTextAreaText ).pause( 1000 )
				.clickOn( "#left-color-picker" ).pause( 1000 )
				.moveBy( 60, 40 ).click().pause( 1000 )
				.clickOn( "#fx-input" )
				.type( fxInputText )
				.moveBy( 100, 0 ).pause( 500 )

		assertThat swfx[ "text-area" ], hasText( swingTextAreaText )
		assertThat swfx[ "#fx-input" ], hasText( fxInputText )
		assertThat swfx[ "#left-color-picker" ], hasValue( textLeftColor )
	}

	@Test
	void "Swinger can use custom selectors"( ) {
		def customSelectors = [
				"editable-textarea": new SimpleSwingerSelector() {
					@Override
					boolean matches( String selector, Component c ) {
						c instanceof JTextArea && c.editable
					}
				} ]

		Swinger swinger = Swinger.forSwingWindow()
		swinger.selectors = Swinger.DEFAULT_SELECTORS + customSelectors

		swinger.clickOn( 'editable-textarea' )
		25.times { swinger.type( KeyEvent.VK_BACK_SPACE ) }
		swinger.type( 'Hello World' ).pause( 100 )

		assertThat( swinger[ "text-area" ], hasText( 'Hello World' ) )
	}

	Color getTextLeftColor( ) {
		def javaFxText = jfxPanel.scene.lookup( "#fx-text" ) as Text
		( javaFxText.fill as LinearGradient ).stops[ 0 ].color
	}

}

