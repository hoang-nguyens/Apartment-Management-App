package utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import models.apartment.Apartment;
import models.resident.Resident;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class PdfExporter {

    public static void exportApartmentListToPDF(List<Apartment> apartmentList, String filePath) {
        Document document = new Document();
        try {
            System.out.println("Bắt đầu xuất PDF");

            // Tạo PdfWriter với đường dẫn lưu file
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Lấy font từ thư mục resources/fonts/
            String fontPath = PdfExporter.class.getClassLoader().getResource("view/arial.ttf").getPath();

            if (fontPath == null) {
                System.out.println("Font không được tìm thấy!");
                return;
            }

            // Sử dụng BaseFont.createFont để tạo font từ file
            BaseFont baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font titleFont = new Font(baseFont, 18, Font.BOLD);
            Font tableHeaderFont = new Font(baseFont, 12, Font.BOLD);
            Font cellFont = new Font(baseFont, 10);

            // Thêm tiêu đề
            Paragraph title = new Paragraph("Danh sách căn hộ", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            System.out.println("Thêm tiêu đề vào tài liệu");

            // Tạo bảng với 8 cột
            PdfPTable table = new PdfPTable(8); // 8 cột
            table.setWidthPercentage(100);

            // Header
            String[] headers = {"ID", "Tầng", "Phòng", "Chủ hộ", "Diện tích", "Xe máy", "Ô tô", "Ngủ/Tắm"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, tableHeaderFont));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            System.out.println("Tổng số căn hộ: " + (apartmentList != null ? apartmentList.size() : "null"));

            // Dữ liệu
            if (apartmentList != null && !apartmentList.isEmpty()) {
                for (Apartment apt : apartmentList) {
                    if (apt == null) {
                        System.out.println("Căn hộ null được bỏ qua");
                        continue;
                    }
                    System.out.println("Đang xử lý căn hộ ID: " + apt.getId());

                    table.addCell(new Phrase(String.valueOf(apt.getId()), cellFont));
                    table.addCell(new Phrase(String.valueOf(apt.getFloor()), cellFont));
                    table.addCell(new Phrase(apt.getRoomNumber(), cellFont));
                    table.addCell(new Phrase(apt.getOwner() != null ? apt.getOwner().getResident().getHoTen().toString() : "Chưa có", cellFont));
                    table.addCell(new Phrase(String.valueOf(apt.getArea()), cellFont));
                    table.addCell(new Phrase(apt.getMotorbikeCount() != null ? String.valueOf(apt.getMotorbikeCount()) : "-", cellFont));
                    table.addCell(new Phrase(apt.getCarCount() != null ? String.valueOf(apt.getCarCount()) : "-", cellFont));
                    table.addCell(new Phrase(apt.getBedroomCount() + "/" + apt.getBathroomCount(), cellFont));
                }
            } else {
                System.out.println("Danh sách căn hộ rỗng hoặc null");
            }

            // Thêm bảng vào tài liệu
            document.add(table);
            document.close();

            System.out.println("Xuất PDF thành công tại: " + filePath);
        } catch (Exception e) {
            System.out.println("Lỗi khi xuất PDF: " + e.getMessage());
            e.printStackTrace();  // In chi tiết lỗi
        }
    }

    public static void exportResidentListToPDF(List<Resident> residentList, String filePath) {
        Document document = new Document();
        try {
            System.out.println("Bắt đầu xuất PDF");

            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            String fontPath = PdfExporter.class.getClassLoader().getResource("view/arial.ttf").getPath();

            if (fontPath == null) {
                System.out.println("Font không được tìm thấy!");
                return;
            }

            BaseFont baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font titleFont = new Font(baseFont, 18, Font.BOLD);
            Font tableHeaderFont = new Font(baseFont, 12, Font.BOLD);
            Font cellFont = new Font(baseFont, 10);

            Paragraph title = new Paragraph("Danh sách cư dân", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Tăng số cột từ 6 lên 7
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);

            // Header mới với cột "Trạng thái tạm trú"
            String[] headers = {"ID", "Tên người dùng", "Email", "Số căn hộ", "Trạng thái xác thực", "Trạng thái tạm trú", "Lần cuối đăng nhập"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, tableHeaderFont));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            System.out.println("Tổng số cư dân: " + (residentList != null ? residentList.size() : "null"));

            if (residentList != null && !residentList.isEmpty()) {
                for (Resident resident : residentList) {
                    if (resident == null) {
                        System.out.println("Cư dân null được bỏ qua");
                        continue;
                    }
                    System.out.println("Đang xử lý cư dân ID: " + resident.getId());

                    table.addCell(new Phrase(String.valueOf(resident.getId()), cellFont));
                    table.addCell(new Phrase(resident.getUsername(), cellFont));
                    table.addCell(new Phrase(resident.getUser().getEmail(), cellFont));
                    table.addCell(new Phrase(resident.getSoPhong() != null ? resident.getSoPhong().toString() : "-", cellFont));
                    table.addCell(new Phrase(resident.getTrangThaiXacThuc().toString(), cellFont));
                    table.addCell(new Phrase(resident.getTrangThaiTamVang() != null ? resident.getTrangThaiTamVang().toString() : "-", cellFont)); // thêm dòng này
                    table.addCell(new Phrase(resident.getUpdatedAt() != null ? resident.getUpdatedAt().toString() : "Chưa đăng nhập", cellFont));
                }
            } else {
                System.out.println("Danh sách cư dân rỗng hoặc null");
            }

            document.add(table);
            document.close();

            System.out.println("Xuất PDF thành công tại: " + filePath);
        } catch (Exception e) {
            System.out.println("Lỗi khi xuất PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
