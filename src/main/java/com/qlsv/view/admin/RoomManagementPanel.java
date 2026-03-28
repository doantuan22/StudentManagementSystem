package com.qlsv.view.admin;

import com.qlsv.controller.RoomController;
import com.qlsv.model.Room;
import com.qlsv.utils.DisplayTextUtil;
import com.qlsv.view.common.AbstractCrudPanel;
import com.qlsv.view.common.DetailSectionPanel;
import com.qlsv.view.dialog.RoomFormDialog;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.util.List;

public class RoomManagementPanel extends AbstractCrudPanel<Room> {

    private final RoomController roomController = new RoomController();
    private final DetailSectionPanel detailSectionPanel = new DetailSectionPanel(
            "Chi tiết phòng học",
            "Vui lòng chọn phòng học để xem chi tiết."
    );

    private final JTextField searchField = new JTextField(15);
    private boolean isSearching = false;

    public RoomManagementPanel() {
        super("Quản lý phòng học");
        setFilterPanel(buildFilterPanel());
        setDetailPanel(detailSectionPanel);
        refreshData();
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Mã phòng", "Tên phòng"};
    }

    @Override
    protected List<Room> loadItems() {
        if (isSearching && !searchField.getText().trim().isBlank()) {
            return roomController.searchRooms(searchField.getText().trim());
        }
        return roomController.getAllRooms();
    }

    @Override
    protected Object[] toRow(Room item) {
        return new Object[]{
                item.getId(),
                item.getRoomCode(),
                item.getRoomName()
        };
    }

    @Override
    protected String getEmptyStateMessage() {
        return isSearching ? "Không tìm thấy phòng học nào phù hợp với từ khóa." : "Chưa có dữ liệu phòng học.";
    }

    @Override
    protected void onSelectionChanged(Room selectedItem) {
        if (selectedItem == null) {
            detailSectionPanel.showMessage("Vui lòng chọn phòng học để xem chi tiết.");
            return;
        }

        detailSectionPanel.showFields(new String[][]{
                {"Mã phòng", DisplayTextUtil.defaultText(selectedItem.getRoomCode())},
                {"Tên phòng", DisplayTextUtil.defaultText(selectedItem.getRoomName())}
        });
    }

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

    @Override
    protected void saveEntity(Room item) {
        roomController.saveRoom(item);
    }

    @Override
    protected void deleteEntity(Room item) {
        roomController.deleteRoom(item.getId());
    }

    private JPanel buildFilterPanel() {
        JButton searchButton = new JButton("Tìm kiếm");
        JButton resetButton = new JButton("Đặt lại");

        searchButton.addActionListener(event -> {
            isSearching = true;
            refreshData();
        });

        resetButton.addActionListener(event -> {
            isSearching = false;
            searchField.setText("");
            refreshData();
        });

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Tra cứu phòng học"),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        filterPanel.add(new JLabel("Từ khóa (nhập mã, tên):"));
        filterPanel.add(searchField);
        filterPanel.add(searchButton);
        filterPanel.add(resetButton);

        return filterPanel;
    }
}
