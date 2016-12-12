package entitys;

import javax.persistence.*;

/**
 * Created by Mr.Z on 2016/5/14 0014.
 */
@Entity
@Table(name = "passenger", schema = "mydb", catalog = "")
public class PassengerEntity {
    private int id;
    private String passengerInfo;

    @Id
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "passenger_info")
    public String getPassengerInfo() {
        return passengerInfo;
    }

    public void setPassengerInfo(String passengerInfo) {
        this.passengerInfo = passengerInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PassengerEntity that = (PassengerEntity) o;

        if (id != that.id) return false;
        if (passengerInfo != null ? !passengerInfo.equals(that.passengerInfo) : that.passengerInfo != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (passengerInfo != null ? passengerInfo.hashCode() : 0);
        return result;
    }
}
