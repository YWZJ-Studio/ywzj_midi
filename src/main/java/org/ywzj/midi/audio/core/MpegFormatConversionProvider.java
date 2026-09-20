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
                    // 单声道
                    new AudioFormat(MPEG1L3, -1.0F, -1, 1, -1, -1.0F, false),
                    new AudioFormat(MPEG1L3, -1.0F, -1, 1, -1, -1.0F, true),
                    // 立体声
                    new AudioFormat(MPEG1L3, -1.0F, -1, 2, -1, -1.0F, false),
                    new AudioFormat(MPEG1L3, -1.0F, -1, 2, -1, -1.0F, true),
            };

    private static final AudioFormat[] OUTPUT_FORMATS =
            {
                    // 单声道，16 位有符号采样
                    new AudioFormat(PCM_SIGNED, -1.0F, 16, 1, 2, -1.0F, false),
                    new AudioFormat(PCM_SIGNED, -1.0F, 16, 1, 2, -1.0F, true),
                    // 立体声，16 位有符号采样
                    new AudioFormat(PCM_SIGNED, -1.0F, 16, 2, 4, -1.0F, false),
                    new AudioFormat(PCM_SIGNED, -1.0F, 16, 2, 4, -1.0F, true),
            };

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

    /** 兼容已指定帧率或帧大小的 MPEG 源格式。 */
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
