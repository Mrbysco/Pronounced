package com.mrbysco.pronounced.client.speech;

import com.mrbysco.pronounced.Pronounced;
import com.mrbysco.pronounced.network.CastSpellMessage;
import com.mrbysco.pronounced.network.PacketHandler;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.result.WordResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

// Credit to SpeechToSpell https://github.com/Agent59/SpeechToSpell/blob/master/src/main/java/net/agent59/stp/speech/Sphinx4SpeechThread.java
public class SphinxThread implements Runnable {
	public static final Logger LOGGER = LogManager.getLogger(Pronounced.MOD_ID + "_SPEECH");
	// other values for audio format won't work (https://cmusphinx.github.io/wiki/tutorialsphinx4/#streamspeechrecognizer)
	private final AudioFormat FORMAT = new AudioFormat(16000.0f, 16, 1, true, false);
	private final TargetDataLine mic = AudioSystem.getTargetDataLine(FORMAT);
	private final AudioInputStream inputStream = new AudioInputStream(mic);
	private CustomSpeechRecognizer recognizer;
	private volatile boolean listeningState = false; // used to check if the speech thread has reached a point, where it can be stopped
	private Player user = null;
	private static SphinxThread instance = null;

	private SphinxThread() throws LineUnavailableException {
	}

	public static SphinxThread getInstance() {
		if (instance == null) {
			try {
				instance = new SphinxThread();
			} catch (LineUnavailableException e) {
				throw new RuntimeException(e);
			}
		}
		return instance;
	}

	@Override
	public void run() {
		LOGGER.info("SPEECH THREAD STARTING");
		try {
			Configuration configuration = new Configuration();

			ClassLoader classLoader = Pronounced.class.getClassLoader();
			URL modelPath = classLoader.getResource("edu/cmu/sphinx/models/en-us/en-us");
			if (modelPath != null)
				configuration.setAcousticModelPath(modelPath.toString());
			else
				LOGGER.error("Unable to locate en_us Acoustic Model inside Pronounced jar");

			URL dicPath = classLoader.getResource("assets/pronounced/speech/2814.dic");
			if (dicPath != null)
				configuration.setDictionaryPath(dicPath.toString());
			else
				LOGGER.error("Unable to locate 2814.dic inside Pronounced jar");

			URL lmPath = classLoader.getResource("assets/pronounced/speech/2814.lm");
			if (lmPath != null)
				configuration.setLanguageModelPath(lmPath.toString());
			else
				LOGGER.error("Unable to locate 2814.lm inside Pronounced jar");

			configuration.setUseGrammar(false);

			recognizer = new CustomSpeechRecognizer(configuration);
			mic.open();

			// This fixes the accumulating audio issue on some Linux systems
			mic.start();
			mic.stop();
			mic.flush();

			recognizer.startRecognition(inputStream);
			listeningState = true;

			SpeechResult speechResult;
			while ((speechResult = recognizer.getResult()) != null) {
				String voice_command = speechResult.getHypothesis();
				for (WordResult word : speechResult.getWords()) {
					System.out.println(word);
				}
				LOGGER.error("VOICE COMMAND: " + voice_command);

				// voice_command is upperCase, so it has to be converted to every Word starting with upperCase
				// and the rest to lowercase
				String[] strings = voice_command.split(" ");
				String spellString = "";

				for (String string : strings) {
					string = string.charAt(0) + string.substring(1).toLowerCase() + " ";
					spellString = spellString.concat(string);
				}
				spellString = spellString.trim();

				user.sendSystemMessage(Component.literal(spellString));

				PacketHandler.CHANNEL.sendToServer(new CastSpellMessage(spellString, this.user.getUUID()));
			}

		} catch (LineUnavailableException | IOException e) {
			LOGGER.info("EXCEPTION " + Arrays.toString(e.getStackTrace()));
			throw new RuntimeException(e);
		} catch (NullPointerException e) {
			LOGGER.info("THE FOLLOWING EXCEPTION WAS CAUSED WHILE STOPPING THE SPEECH THREAD" +
					" AND WAS PROBABLY INTENDED:\n\t" + Arrays.toString(e.getStackTrace()));
			e.printStackTrace();
		}
		LOGGER.info("SPEECH THREAD ENDING");
	}

	public void end() {
		try {
			while (!listeningState) {
				Thread.onSpinWait();
			}
			inputStream.close();
			mic.stop();
			mic.flush();
			recognizer.cancelRecognition();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void pauseRecognition() {
		mic.stop();
		LOGGER.error("PAUSING RECOGNITION");
	}

	public void resumeRecognition(Player player) {
		user = player;
		mic.flush();
		mic.start();
		LOGGER.error("RESUMING RECOGNITION");
	}
}