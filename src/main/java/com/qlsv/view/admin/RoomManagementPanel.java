/**
 * Màn hình quản trị cho quản lý phòng.
 */
package com.qlsv.view.admin;

import com.qlsv.controller.RoomController;
import com.qlsv.model.Room;
import com.qlsv.view.common.AbstractCrudPanel;
import com.qlsv.view.dialog.RoomFormDialog;

import javax.swing.JTextField;
import java.util.List;

public class RoomManagementPanel extends AbstractCrudPanel<Room> {

    private final RoomController roomController = new RoomController();
    private final JTextField searchField = new JTextField(15);

    private boolean isSearching;

    /**
     * Khởi tạo quản lý phòng.
     */
    public RoomManagementPanel() {
        super("Quản lý phòng học");
        refreshData();
    }

    /**
     * Kiểm tra đầu trang tìm kiếm visible.
     */
    @Override
    protected boolean isHeaderSearchVisible() {
        return false;
    }

    /**
     * Trả về column names.
     */
    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Mã phòng", "Tên phòng"};
    }

    /**
     * Nạp items.
     */
    @Override
    protected List<Room> loadItems() {
        if (isSearching && !searchField.getText().trim().isBlank()) {
            return roomController.searchRooms(searchField.getText().trim());
        }
        return roomController.getAllRooms();
    }

    /**
     * Xử lý to row.
     */
    @Override
    protected Object[] toRow(Room item) {
        return new Object[]{
                item.getId(),
                item.getRoomCode(),
                item.getRoomName()
        };
    }

    /**
     * Trả về trạng thái trống thông báo.
     */
    @Override
    protected String getEmptyStateMessage() {
        return isSearching ? "Không tìm thấy phòng học nào phù hợp với từ khóa." : "Chưa có dữ liệu phòng học.";
    }

    /**
     * Xử lý prompt for entity.
     */
    @Override
    protected Room promptForEntity(Room existingItem) {
        RoomFormDialog.RoomFormResult formResult = RoomFormDialog.showDialog(
                this,
                new RoomFormDialog.RoomFormModel(
                        existingItem == null ? "Thêm phòng học" : "Cập nhật phòng học",
                        existingItem == null ? "" : existingItem.getRoomCode(),
                        existingItem == null ? "" : existingItem.getRoomName()
                )
        );

        if (formResult == null) {
            return null;
        }

        Room room = existingItem == null ? new Room() : existingItem;
        room.setRoomCode(formResult.roomCode().trim());
        room.setRoomName(formResult.roomName().trim());
        return room;
    }

    /**
     * Lưu entity.
     */
    @Override
    protected void saveEntity(Room item) {
        roomController.saveRoom(item);
    }

    /**
     * Xóa entity.
     */
    @Override
    protected void deleteEntity(Room item) {
        roomController.deleteRoom(item.getId());
    }
}
