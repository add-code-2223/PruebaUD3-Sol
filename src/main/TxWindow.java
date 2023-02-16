package main;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import modelo.Departamento;
import modelo.servicio.DepartamentoServicio;
import modelo.servicio.IDepartamentoServicio;

public class TxWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JPanel contentPane;

	private JTextArea mensajes_text_Area;
	private JList<Departamento> JListAllDepts;

	private IDepartamentoServicio departamentoServicio;
	private CreateNewDeptDialog createDialog;
	private JButton btnModificarDepartamento;
	private JButton btnEliminarDepartamento;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TxWindow frame = new TxWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public TxWindow() {

		departamentoServicio = new DepartamentoServicio();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 847, 772);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel panel = new JPanel();
		panel.setBounds(8, 8, 821, 500);
		contentPane.add(panel);
		panel.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(19, 264, 669, 228);
		panel.add(scrollPane);

		mensajes_text_Area = new JTextArea();
		scrollPane.setViewportView(mensajes_text_Area);
		mensajes_text_Area.setEditable(false);
		mensajes_text_Area.setText("Panel de mensajes");
		mensajes_text_Area.setForeground(new Color(255, 0, 0));
		mensajes_text_Area.setFont(new Font("Monospaced", Font.PLAIN, 13));

		JButton btnShowAllDepts = new JButton("Mostrar departamentos");

		btnShowAllDepts.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnShowAllDepts.setBounds(50, 37, 208, 36);
		panel.add(btnShowAllDepts);

		JScrollPane scrollPanel_in_JlistAllDepts = new JScrollPane();
		panel.add(scrollPanel_in_JlistAllDepts);

		btnModificarDepartamento = new JButton("Modificar departamento");

		JListAllDepts = new JList<Departamento>();

		JListAllDepts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		panel.add(JListAllDepts);
		JListAllDepts.setBounds(403, 37, 377, 200);

		JButton btnCrearNuevoDepartamento = new JButton("Crear nuevo departamento");
		
		btnCrearNuevoDepartamento.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnCrearNuevoDepartamento.setBounds(50, 85, 208, 36);
		panel.add(btnCrearNuevoDepartamento);

		btnModificarDepartamento.setEnabled(false);
		btnModificarDepartamento.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnModificarDepartamento.setBounds(50, 139, 208, 36);
		panel.add(btnModificarDepartamento);

		btnEliminarDepartamento = new JButton("Eliminar departamento");

		btnEliminarDepartamento.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnEliminarDepartamento.setEnabled(false);
		btnEliminarDepartamento.setBounds(50, 201, 208, 36);
		panel.add(btnEliminarDepartamento);
		
		//Eventos
		ActionListener showAllDepartamentosActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getAllDepartamentos();
			}
		};
		btnShowAllDepts.addActionListener(showAllDepartamentosActionListener);

		ActionListener crearListener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				JFrame owner = (JFrame) SwingUtilities.getRoot((Component) e.getSource());
				createDialog = new CreateNewDeptDialog(owner, "Crear nuevo departamento",
						Dialog.ModalityType.DOCUMENT_MODAL, null);
				showDialog();
			}
		};
		btnCrearNuevoDepartamento.addActionListener(crearListener);
		
		ActionListener modificarListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedIx = JListAllDepts.getSelectedIndex();
				if (selectedIx > -1) {
					Departamento departamento = (Departamento) JListAllDepts.getModel().getElementAt(selectedIx);
					if (departamento != null) {

						JFrame owner = (JFrame) SwingUtilities.getRoot((Component) e.getSource());

						createDialog = new CreateNewDeptDialog(owner, "Modificar departamento",
								Dialog.ModalityType.DOCUMENT_MODAL, departamento);
						showDialog();
					}
				}
			}
		};
		
		btnModificarDepartamento.addActionListener(modificarListener);

		ListSelectionListener selectionListListener = new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					int selectedIx = JListAllDepts.getSelectedIndex();
					btnModificarDepartamento.setEnabled((selectedIx > -1));
					btnEliminarDepartamento.setEnabled((selectedIx > -1));
					if (selectedIx > -1) {
						Departamento d = (Departamento) TxWindow.this.JListAllDepts.getModel().getElementAt(selectedIx);
						if (d != null) {
							addMensaje(true, "Se ha seleccionado el d: " + d);
						}
					}
				}
			}
		};
		JListAllDepts.addListSelectionListener(selectionListListener);

		ActionListener deleteListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedIx = JListAllDepts.getSelectedIndex();
				if (selectedIx > -1) {
					Departamento d = (Departamento) JListAllDepts.getModel().getElementAt(selectedIx);
					if (d != null) {
						try {
							boolean exito = departamentoServicio.delete(d.getDeptno());
							if (exito) {
								addMensaje(true, "Se ha eliminado el dept con id: " + d.getDeptno());
								getAllDepartamentos();
							}
						} catch (exceptions.InstanceNotFoundException e1) {
							addMensaje(true, "No se ha podido borrar el departamento. No se ha encontrado con id: "
									+ d.getDeptno());
						} catch (Exception ex) {
							addMensaje(true, "No se ha podido borrar el departamento. ");
							System.out.println("Exception: " + ex.getMessage());
							ex.printStackTrace();
						}
					}
				}
			}
		};
		btnEliminarDepartamento.addActionListener(deleteListener);
	}

	private void addMensaje(boolean keepText, String msg) {
		String oldText = "";
		if (keepText) {
			oldText = mensajes_text_Area.getText();

		}
		oldText = oldText + "\n" + msg;
		mensajes_text_Area.setText(oldText);

	}

	private void showDialog() {
		createDialog.setVisible(true);
		Departamento departamentoACrear = createDialog.getResult();
		if (departamentoACrear != null) {

			saveOrUpdate(departamentoACrear);
		}
	}

	private void saveOrUpdate(Departamento dept) {
		try {
			Departamento nuevo = departamentoServicio.saveOrUpdate(dept);
			if (nuevo != null) {
				addMensaje(true, "Se ha creado un departamento con id: " + nuevo.getDeptno());
				getAllDepartamentos();
			} else {
				addMensaje(true, " El departamento no se ha creado/actualizado correctamente");
			}

		} catch (Exception ex) {
			addMensaje(true, "Ha ocurrido un error y no se ha podido crear el departamento");
		}
	}

	private void getAllDepartamentos() {
		List<Departamento> departamentos = departamentoServicio.getAll();
		addMensaje(true, "Se han recuperado: " + departamentos.size() + " departamentos");
		DefaultListModel<Departamento> defModel = new DefaultListModel<>();

		defModel.addAll(departamentos);

		JListAllDepts.setModel(defModel);

	}

}
