package org.ywzj.midi.util;

public class MidiUtils {

    private static final String[] simpleNotation = {"a", "as", "b", "c", "cs", "d", "ds", "e", "f", "fs", "g", "gs"};

    public static String noteToNotation(int note) {
        int octave = (note + 3) / 12 - 1;
        int noteIndex = (note + 3) % 12;
        return simpleNotation[noteIndex] + octave;
    }

    public static int notationToNote(String notation) {
        if (notation.length() != 2 && notation.length() != 3) {
            return 0;
        }
        String noteChar;
        int octave;
        if (notation.length() == 2) {
            noteChar = notation.substring(0, 1);
            octave = Integer.parseInt(notation.substring(1));
        } else {
            noteChar = notation.substring(0, 2);
            octave = Integer.parseInt(notation.substring(2));
        }
        int noteIndex = switch (noteChar) {
            case "a" -> 0;
            case "as" -> 1;
            case "b" -> 2;
            case "c" -> 3;
            case "cs" -> 4;
            case "d" -> 5;
            case "ds" -> 6;
            case "e" -> 7;
            case "f" -> 8;
            case "fs" -> 9;
            case "g" -> 10;
            case "gs" -> 11;
            default -> throw new IllegalArgumentException("Invalid notation: " + notation);
        };
        return (octave + 1) * 12 + noteIndex - 3;
    }

    public static int keyCodeToIndex(int keyCode) {
        return switch (keyCode) {
            case 49 -> 0;
            case 50 -> 1;
            case 51 -> 2;
            case 52 -> 3;
            case 53 -> 4;
            case 54 -> 5;
            case 55 -> 6;
            case 56 -> 7;
            case 57 -> 8;
            case 48 -> 9;
            case 45 -> 10;
            case 61 -> 11;
            case 81 -> 12;
            case 87 -> 13;
            case 69 -> 14;
            case 82 -> 15;
            case 84 -> 16;
            case 89 -> 17;
            case 85 -> 18;
            case 73 -> 19;
            case 79 -> 20;
            case 80 -> 21;
            case 91 -> 22;
            case 93 -> 23;
            default -> -1;
        };
    }

    public static String velocitySuffix(int velocity) {
        if (velocity <= 40) {
            return "_p";
        } else if (velocity > 80) {
            return "_f";
        }
        return "";
    }

    public static float velocityVolume(int velocity) {
        if (velocity > 80) {
            return velocity / 127f;
        } else if (velocity > 40) {
            return velocity / 80f;
        }
        return velocity / 40f;
    }

    public static void main(String args[]) {
        int note = notationToNote("a5");
        System.out.println(note);
        String notation = noteToNotation(note);
        System.out.println(notation);
    }

}
