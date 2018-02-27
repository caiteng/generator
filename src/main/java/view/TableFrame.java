package view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import util.GenerateAllFromSQL;

/**
 * @author cait
 */
public class TableFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel panel;
	private JTable table;
	private JButton btn;

	public TableFrame(String frameName, List<String> list) {
		InitialComponent(list);
		setTitle(frameName);
		setVisible(true);
	}

	private void InitialComponent(List<String> list) {
		setLayout(null);
		setSize(400, 700);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		panel = new JPanel();
		panel.setSize(this.getWidth(), this.getHeight());
		panel.setLocation(0, 0);
		panel.setLayout(null);
		String[][] strs = new String[list.size()][list.size()];
		for (int i = 0; i < list.size(); i++) {
			strs[i][0] = list.get(i);
		}
		table = new JTable(new DefaultTableModel(strs, new String[] { "表名", "选择" }) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		});
		table.getColumnModel().getColumn(1).setCellRenderer(new TableCellRenderer() {
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				JCheckBox ck = new JCheckBox();
				ck.setSelected(isSelected);
				ck.setOpaque(false);
				ck.setHorizontalAlignment((int) 0.5f);
				return ck;
			}
		});
		table.setSize(panel.getWidth(), panel.getHeight() - 80);
		table.setLocation(0, 0);

		btn = new JButton("生成文件...");
		btn.setSize(80, 40);
		btn.setLocation((panel.getWidth()) / 2 - 40, panel.getHeight() - 80);

		// 按钮
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List list = new ArrayList();
				for (int rowindex : table.getSelectedRows()) {
					// JOptionPane.showMessageDialog(null, table.getValueAt(rowindex, 0));
					list.add(table.getValueAt(rowindex, 0));
				}
				try {
					new GenerateAllFromSQL(list);
					// 关闭页面，通知完成
					JOptionPane.showMessageDialog(null, "完成");
					dispose();
				} catch (ClassNotFoundException e1) {
					JOptionPane.showMessageDialog(null, "失败");
					e1.printStackTrace();
				} catch (SQLException e1) {
					JOptionPane.showMessageDialog(null, "失败");
					e1.printStackTrace();
				}
			}
		});

		panel.add(table);
		panel.add(btn);
		this.add(panel);
		//Jbutton偶尔无法渲染，加上刷新保证按钮一定出现
		SwingUtilities.updateComponentTreeUI(this);

	}

}