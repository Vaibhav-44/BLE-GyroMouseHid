package com.example.gyromouse

object HidReportDescriptor {
    // From https://www.usb.org/sites/default/files/documents/hid1_11.pdf (Appendix E.10)
    val MOUSE_REPORT_DESCRIPTOR: ByteArray = byteArrayOf(
        0x05, 0x01,  // Usage Page (Generic Desktop)
        0x09, 0x02,  // Usage (Mouse)
        0xA1.toByte(), 0x01,  // Collection (Application)
        0x09, 0x01,  //   Usage (Pointer)
        0xA1.toByte(), 0x00,  //   Collection (Physical)
        // Buttons
        0x05, 0x09,  //     Usage Page (Buttons)
        0x19, 0x01,  //     Usage Minimum (1)
        0x29, 0x03,  //     Usage Maximum (3)
        0x15, 0x00,  //     Logical Minimum (0)
        0x25, 0x01,  //     Logical Maximum (1)
        0x95.toByte(), 0x03,  //     Report Count (3)
        0x75, 0x01,  //     Report Size (1)
        0x81.toByte(), 0x02,  //     Input (Data, Var, Abs)
        0x95.toByte(), 0x01,  //     Report Count (1)
        0x75, 0x05,  //     Report Size (5)
        0x81.toByte(), 0x01,  //     Input (Const)
        // X, Y
        0x05, 0x01,  //     Usage Page (Generic Desktop)
        0x09, 0x30,  //     Usage (X)
        0x09, 0x31,  //     Usage (Y)
        0x15, 0x81.toByte(),  //     Logical Minimum (-127)
        0x25, 0x7F,  //     Logical Maximum (127)
        0x75, 0x08,  //     Report Size (8)
        0x95.toByte(), 0x02,  //     Report Count (2)
        0x81.toByte(), 0x06,  //     Input (Data, Var, Rel)
        // Wheel
        0x09, 0x38,  //     Usage (Wheel)
        0x15, 0x81.toByte(),  //     Logical Minimum (-127)
        0x25, 0x7F,  //     Logical Maximum (127)
        0x75, 0x08,  //     Report Size (8)
        0x95.toByte(), 0x01,  //     Report Count (1)
        0x81.toByte(), 0x06,  //     Input (Data, Var, Rel)
        0xC0.toByte(),       //   End Collection
        0xC0.toByte()        // End Collection
    )
}
