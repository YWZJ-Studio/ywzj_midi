package org.ywzj.midi.audio.core;

import javazoom.spi.mpeg.sampled.convert.DecodedMpegAudioInputStream;
import javazoom.spi.mpeg.sampled.file.MpegEncoding;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.Encodings;
import org.tritonus.share.sampled.convert.TEncodingFormatConversionProvider;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.util.Arrays;

public class MpegFormatConversionProvider extends TEncodingFormatConversionProvider {
    private static final AudioFormat.Encoding MPEG1L3 = Encodings.getEncoding("MPEG1L3");
    private static final AudioFormat.Encoding PCM_SIGNED = Encodings.getEncoding("PCM_SIGNED");

    private static final AudioFormat[] INPUT_FORMATS =
            {
                    // mono
                    new AudioFormat(MPEG1L3, -1.0F, -1, 1, -1, -1.0F, false),
                    new AudioFormat(MPEG1L3, -1.0F, -1, 1, -1, -1.0F, true),
                    // stereo
                    new AudioFormat(MPEG1L3, -1.0F, -1, 2, -1, -1.0F, false),
                    new AudioFormat(MPEG1L3, -1.0F, -1, 2, -1, -1.0F, true),
            };


    private static final AudioFormat[] OUTPUT_FORMATS =
            {
                    // mono, 16 bit signed
                    new AudioFormat(PCM_SIGNED, -1.0F, 16, 1, 2, -1.0F, false),
                    new AudioFormat(PCM_SIGNED, -1.0F, 16, 1, 2, -1.0F, true),
                    // stereo, 16 bit signed
                    new AudioFormat(PCM_SIGNED, -1.0F, 16, 2, 4, -1.0F, false),
                    new AudioFormat(PCM_SIGNED, -1.0F, 16, 2, 4, -1.0F, true),
            };

    /**
     * Constructor.
     */
    public MpegFormatConversionProvider() {
        super(Arrays.asList(INPUT_FORMATS), Arrays.asList(OUTPUT_FORMATS));
        if (TDebug.TraceAudioConverter) {
            TDebug.out(">MpegFormatConversionProvider()");
        }
    }

    public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream audioInputStream) {
        if (TDebug.TraceAudioConverter) {
            TDebug.out(">MpegFormatConversionProvider.getAudioInputStream(AudioFormat targetFormat, AudioInputStream audioInputStream):");
        }
        return new DecodedMpegAudioInputStream(targetFormat, audioInputStream);
    }

    /**
     * Add conversion support for any MpegEncoding source with FrameRate or FrameSize not empty.
     *
     * @param targetFormat
     * @param sourceFormat
     * @return
     */
    public boolean isConversionSupported(AudioFormat targetFormat, AudioFormat sourceFormat) {
        if (TDebug.TraceAudioConverter) {
            TDebug.out(">MpegFormatConversionProvider.isConversionSupported(AudioFormat targetFormat, AudioFormat sourceFormat):");
            TDebug.out("checking if conversion possible");
            TDebug.out("from: " + sourceFormat);
            TDebug.out("to: " + targetFormat);
        }

        boolean conversion = super.isConversionSupported(targetFormat, sourceFormat);
        if (!conversion) {
            AudioFormat.Encoding enc = sourceFormat.getEncoding();
            if (enc instanceof MpegEncoding) {
                if ((sourceFormat.getFrameRate() != AudioSystem.NOT_SPECIFIED) || (sourceFormat.getFrameSize() != AudioSystem.NOT_SPECIFIED)) {
                    conversion = true;
                }
            }
        }
        return conversion;
    }

}
