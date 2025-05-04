package utils;

import models.enums.SoPhong;

import java.util.Arrays;

public class SoPhongUtil {
    public static Boolean soPhongHopLe(String soPhong) {
        return Arrays.stream(SoPhong.values())
                .anyMatch(roomNumber -> roomNumber.getMaSo().equalsIgnoreCase(soPhong));
    }
}
