package com.bluebear.cinemax.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "Room")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RoomID")
    private int roomId;


    @Column(name = "TheaterID", nullable = false)
    private int theater;

    @Column(name = "Name", length = 10, nullable = false)
    private String name;

    @Column(name = "Collumn", nullable = false)
    private int column;

    @Column(name = "Row", nullable = false)
    private int row;

    @Column(name = "TypeOfRoom", length = 20)
    private String typeOfRoom;

    // Constructors
    public Room() {
    }

    public Room(int theater, String name, int column, int row, String typeOfRoom) {
        this.theater = theater;
        this.name = name;
        this.column = column;
        this.row = row;
        this.typeOfRoom = typeOfRoom;
    }

    // Getters and Setters
    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getTheater() {
        return theater;
    }

    public void setTheater(int theater) {
        this.theater = theater;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public String getTypeOfRoom() {
        return typeOfRoom;
    }

    public void setTypeOfRoom(String typeOfRoom) {
        this.typeOfRoom = typeOfRoom;
    }

    // toString Method

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return roomId == room.roomId && theater == room.theater && column == room.column && row == room.row && Objects.equals(name, room.name) && Objects.equals(typeOfRoom, room.typeOfRoom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId, theater, name, column, row, typeOfRoom);
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomId=" + roomId +
                ", theater=" + theater +
                ", name='" + name + '\'' +
                ", column=" + column +
                ", row=" + row +
                ", typeOfRoom='" + typeOfRoom + '\'' +
                '}';
    }
}
