package models.resident;

import jakarta.persistence.*;
import models.BaseModel;
import models.enums.*;
import models.user.User;

import java.time.LocalDate;

@Entity
@Table(name = "nhan_khau")
public class Resident extends BaseModel {

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "username", unique = true, length = 50)
    private String username;


    @Column(name = "ho_ten", nullable = true, length = 50)
    private String hoTen;

    @Column(unique = true, length = 50)
    private String cccd;

    @Column(length = 50)
    private String sdt;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai_xac_thuc", nullable = false)
    private XacThuc trangThaiXacThuc;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    private String trangThaiXacThucHienThi;

    @Enumerated(EnumType.STRING)
    @Column(name = "gioi_tinh", nullable = true)
    private Gender gioiTinh;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai_tam_vang", nullable = true)
    private TamVangStatus trangThaiTamVang;

    @Enumerated(EnumType.STRING) // hoặc EnumType.ORDINAL
    @Column(name = "so_phong_id", nullable = true)
    private SoPhong soPhong;

    // Constructor mặc định
    public Resident() {}

    public Resident(String hoTen,
                    String cccd,
                    String sdt,
                    LocalDate ngaySinh,
                    Gender gioiTinh,
                    TamVangStatus trangThaiTamVang,
                    XacThuc trangThaiXacThuc,
                    SoPhong soPhong,
                    User user) {
        this.hoTen = hoTen;
        this.cccd = cccd;
        this.sdt = sdt;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.trangThaiTamVang = trangThaiTamVang;
        this.trangThaiXacThuc = trangThaiXacThuc;
        this.soPhong = soPhong;
        this.user = user;

    }

    // Getters
    public User getUser() {return user;}

    public String getHoTen() {return hoTen;}

    public String getCccd() {return cccd;}

    public String getSdt() {return sdt;}

    public LocalDate getNgaySinh() {return ngaySinh;}

    public Gender getGioiTinh() {return gioiTinh;}


    public TamVangStatus getTrangThaiTamVang() {return trangThaiTamVang;}

    public XacThuc getTrangThaiXacThuc() { return trangThaiXacThuc;}

    public SoPhong getSoPhong() {
        return soPhong;
    }

    public String getUsername() {
        return user != null ? user.getUsername() : "Chưa xác thực";
    }

    public String getTrangThaiXacThucHienThi() {
        return trangThaiXacThuc != null ? trangThaiXacThuc.toString() : "Chưa xác thực";
    }




    // Setters
    public void setUser(User user) {
        this.user = user;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public void setTrangThaiXacThuc(XacThuc trangThaiXacThuc) {this.trangThaiXacThuc = trangThaiXacThuc;}

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public void setSoPhong(SoPhong soPhong) {
        this.soPhong = soPhong;
    }

    public void setTrangThaiTamVang(TamVangStatus trangThaiTamVang) {
        this.trangThaiTamVang = trangThaiTamVang;
    }

    public void setGioiTinh(Gender gioiTinh) {
        this.gioiTinh = gioiTinh;
    }
}