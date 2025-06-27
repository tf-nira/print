package io.mosip.print.constant;

public enum FingerType {
    RIGHT_THUMB("Right Thumb", 1),
    RIGHT_INDEXFINGER("Right IndexFinger", 2),
    RIGHT_MIDDLEFINGER("Right MiddleFinger", 3),
    RIGHT_RINGFINGER("Right RingFinger", 4),
    RIGHT_LITTLEFINGER("Right LittleFinger", 5),
    LEFT_THUMB("Left Thumb", 6),
    LEFT_INDEXFINGER("Left IndexFinger", 7),
    LEFT_MIDDLEFINGER("Left MiddleFinger", 8),
    LEFT_RINGFINGER("Left RingFinger", 9),
    LEFT_LITTLEFINGER("Left LittleFinger", 10);

    private final String name;
    private final int index;

    FingerType(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public static String getNameByIndex(Integer index) {
        if (index == null) {
            return null;
        }
        
        for (FingerType finger : values()) {
            if (finger.getIndex() == index) {
                return finger.getName();
            }
        }
        
        return null;
    }
}