package repositories;

import models.Resident;
import models.User;
import models.enums.SoPhong;
import models.enums.TamVangStatus;
import models.enums.XacThuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface ResidentRepository extends JpaRepository<Resident, Long> {

    Resident findByUser(User user);


    Resident findByCccd(String cccd);


    List<Resident> findAll();


    List<Resident> findBySoPhong(SoPhong soPhong);

    List<Resident> findByTrangThaiXacThuc(XacThuc trangThaiXacThuc);

    List<Resident> findByTrangThaiTamVang(TamVangStatus trangThaiTamVang);


    List<Resident> findBySoPhongAndTrangThaiTamVang(SoPhong soPhong, TamVangStatus trangThaiTamVang);

    @Query("SELECT r FROM Resident r WHERE " +
            "( :soPhong IS NULL OR r.soPhong = :soPhong ) AND " +
            "( :trangThaiXacThuc IS NULL OR r.trangThaiXacThuc = :trangThaiXacThuc ) AND " +
            "( :tamVangStatus IS NULL OR r.trangThaiTamVang = :tamVangStatus )")
    List<Resident> findByFilters(
            @Param("soPhong") SoPhong soPhong,
            @Param("trangThaiXacThuc") XacThuc trangThaiXacThuc,
            @Param("tamVangStatus") TamVangStatus tamVangStatus);


}