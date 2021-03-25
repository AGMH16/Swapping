/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;

/**
 *
 * @author Miguel Matul <https://github.com/MigueMat4>
 */
public class frmMain extends javax.swing.JFrame {
      Proceso proceso = new Proceso("Sistema Operativo");
   
    private final Memoria RAM = new Memoria();
    private final Graphics g;
    private final DefaultListModel<String> procesos_en_disco = new DefaultListModel<>();
    private static Semaphore mutex = new Semaphore(1,true);

    /**
     * Creates new form frmMain
     */
    public frmMain() {
        initComponents();
        pnlMemoria.setBackground(Color.GRAY);
        g = pnlMemoria.getGraphics();
        pnlMemoria.paintComponents(g);
        txtTablaProcesos.setEditable(false);
        listProcesos.setModel(procesos_en_disco);
       proceso.start();
    }

    public class Proceso extends Thread {

        private int base;
        private int limite;
        private int longitud;
        private final String nombre;

        public Proceso(String name) {
            if (name.equals("Sistema Operativo")) {
                longitud = 20; // 2K
                nombre = name;
            } else {
                longitud = (int) (Math.random() * 10) + 3; //tamaño random que le asignan a el txt
                longitud = longitud * 10; // De 3K a 12K
                nombre = "Proceso " + name;
            }
        }

        @Override
        public void run() {
            File process_logs = new File(this.nombre + "_logs.txt");
            FileWriter primer_registro;
            try {
                primer_registro = new FileWriter(process_logs);
                primer_registro.write("El " + this.nombre + " fue creado el " + LocalDateTime.now());
                primer_registro.close();
            } catch (IOException ex) {
                Logger.getLogger(frmMain.class.getName()).log(Level.SEVERE, null, ex);
            }
           
            
            String texto = "";
            System.out.println("Proceso " + this.nombre + " entrando a la región crítica");
            //VARIABLES COMPARTIDAS
            // tope =
            //siguiente_slot_libre
            int espacio_libre = RAM.tope - RAM.siguiente_slot_libre;// slot_libre es el espacio que ocupa actualmente el proceso
            System.out.println("THE RAM" + RAM.siguiente_slot_libre);
            this.base = RAM.siguiente_slot_libre; // base guarda el tamaño actual del documento
           // System.out.println("LONGITUD--------" + longitud);
            for (int i = 0; i < this.longitud; i++) { //
                System.out.println("Entra RAM" + RAM.siguiente_slot_libre);
               // RAM.slots[i] = "Instrucción de " + this.nombre; // slots guarda en el tamaño actual de cada txt - 3 en arreglo posición 2 

                System.out.println("Aumenta" + RAM.siguiente_slot_libre);
                System.out.println("Entra RAM"+  RAM.siguiente_slot_libre);
                RAM.slots[i] = "Instrucción de " + this.nombre; // slots guarda en el tamaño actual de cada txt - 3 en arreglo posición 2 
                this.limite = RAM.siguiente_slot_libre;
                System.out.println("Va a limite "+ RAM.siguiente_slot_libre);
                RAM.siguiente_slot_libre++;
                System.out.println("Aumenta"+ RAM.siguiente_slot_libre);
            }
            
             
          /*  if(RAM.siguiente_slot_libre>320){
                
                procesos_en_disco.addElement(nombre);
            }*/
            
            RAM.procesos_cargados.add(this);
            texto = this.nombre + " - Registro base: " + (this.base / 10 + 1) + "K";
            System.out.println(texto);
            texto = this.nombre + " - Registro límite: " + (this.limite / 10 + 1) + "K";
            System.out.println(texto);
            texto = this.nombre + " - " + (this.longitud / 10) + "K";
            txtTablaProcesos.setText(txtTablaProcesos.getText() + texto + "\n");
            System.out.println("Proceso " + this.nombre + " saliendo de la región crítica");
            FileWriter segundo_registro;
            Scanner lector;
            String info = "";
            try {
                lector = new Scanner(process_logs);
                while (lector.hasNextLine()) {
                    info = lector.nextLine() + "\n";
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(frmMain.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                segundo_registro = new FileWriter(process_logs);
                segundo_registro.write(info + "El " + this.nombre + " está listo para ser ejecutado");
                segundo_registro.close();
            } catch (IOException ex) {
                Logger.getLogger(frmMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private class Demonio extends Thread{
    
}
    public class Memoria {

        public String[] slots = new String[320];
        private int siguiente_slot_libre = 0;
        private final int tope = 319;
        public List<Proceso> procesos_cargados = new ArrayList<>();
        // Lista con estructura que tiene nodos que guardan las posiciones iniciales y finales de los espacios libres
        
        

        public Memoria() {
            for (int i = 0; i < 320; i++) {
                slots[i] = "";
            }
        }

        public void graficarMemoria() {
            int val = 0, dato = 1, tamanio=32;
            boolean entro = false;
            Iterator<Proceso> iterator = RAM.procesos_cargados.iterator();
            while (iterator.hasNext()) {
                entro = true;
                Proceso process = (Proceso) iterator.next();
              
                if (tamanio >= process.longitud / 10) {

                    g.setColor(Color.BLACK);
                    g.drawRect(0, dato, 170, process.longitud - 1);
                    g.setColor(Color.WHITE);
                    g.fillRect(0, dato, 170, process.longitud - 1);
                    g.setColor(Color.BLACK);
                    dato = dato + process.longitud;
                    val = val + process.longitud / 10;
                    tamanio = 32 - val;
                    System.out.println("tamani" + tamanio);
                    System.out.println("proces" + process.longitud);
                    if (process.nombre.equals("Sistema Operativo")) {
                        g.drawString("Sistema Operativo", 40, 15);
                    } else {
                        g.drawString(process.nombre, 60, dato - process.longitud / 2);

                        /* System.out.println("process longitud******** " + process.longitud);
                    g.drawString(process.nombre, 60, process.base + process.longitud / 2);
                    process.base=process.longitud ;
                    System.out.println("process base-----------" + process.base);
                    int val= process.base + process.longitud / 2;
                    System.out.println("operacion"+val );*/
                    }
                } else {
                    g.setColor(Color.BLACK);
                    g.drawRect(0, dato, 170, process.longitud - 1);
                    g.setColor(Color.GRAY);
                    g.fillRect(0, dato, 170, process.longitud - 1);
                    g.setColor(Color.BLACK);
                    dato = dato + process.longitud;
                    int x = val - process.longitud;
                    System.out.println("dato"+ dato);
                    g.drawString(tamanio + "K", 60, 320-(tamanio*10)/2);
                    procesos_en_disco.addElement(process.nombre);
                }
              
            }

            if (entro == false) {
                g.setColor(Color.BLACK);
                g.drawRect(0, 320, 170, 320);
                g.setColor(Color.GRAY);
                g.fillRect(0, 320, 170, 320);
                g.setColor(Color.BLACK);
                g.drawString(32 + "K", 60, 160);

            }
            
            
            
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        pnlMemoria = new javax.swing.JPanel();
        btnStart = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        listProcesos = new javax.swing.JList<>();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtTablaProcesos = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        btnLoad = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel1.setText("Administrador de Memoria");

        pnlMemoria.setAutoscrolls(true);
        pnlMemoria.setPreferredSize(new java.awt.Dimension(170, 320));

        javax.swing.GroupLayout pnlMemoriaLayout = new javax.swing.GroupLayout(pnlMemoria);
        pnlMemoria.setLayout(pnlMemoriaLayout);
        pnlMemoriaLayout.setHorizontalGroup(
            pnlMemoriaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 170, Short.MAX_VALUE)
        );
        pnlMemoriaLayout.setVerticalGroup(
            pnlMemoriaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 320, Short.MAX_VALUE)
        );

        btnStart.setText("Iniciar");
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        listProcesos.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(listProcesos);

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel2.setText("Memoria RAM");

        txtTablaProcesos.setColumns(20);
        txtTablaProcesos.setRows(5);
        jScrollPane2.setViewportView(txtTablaProcesos);

        jLabel3.setText("32K");

        jLabel4.setText("0K");

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel5.setText("Tabla de procesos");

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel6.setText("Disco duro");

        btnLoad.setText("Graficar");
        btnLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoadActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addGroup(layout.createSequentialGroup()
                .addGap(303, 303, 303)
                .addComponent(btnStart)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel4))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jLabel3)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlMemoria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(46, 46, 46)
                                .addComponent(jLabel5)
                                .addGap(140, 140, 140)
                                .addComponent(jLabel2))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(253, 253, 253)
                                .addComponent(jLabel1)))
                        .addGap(0, 13, Short.MAX_VALUE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(86, 86, 86)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(25, 25, 25))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel6)
                        .addGap(59, 59, 59))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnLoad, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(280, 280, 280))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnStart)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 283, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pnlMemoria, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane1))
                        .addGap(18, 18, 18)
                        .addComponent(btnLoad, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(20, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel3)
                        .addGap(66, 66, 66))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
        // Creación de 10 procesos para cargar en memoria principal
      
        try {
            
            
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(frmMain.class.getName()).log(Level.SEVERE, null, ex);
        }
     //   procesos_en_disco.addElement("fdsaf");
        
        Proceso user_process;
        char letra = 'A';
        for (int i = 0; i < 10; i++) {
            
            user_process = new Proceso(String.valueOf(letra));
            user_process.start();
            letra++;
        }
        btnStart.setEnabled(false);
    }//GEN-LAST:event_btnStartActionPerformed

    private void btnLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadActionPerformed
        RAM.graficarMemoria();
    }//GEN-LAST:event_btnLoadActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(frmMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(frmMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(frmMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(frmMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new frmMain().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLoad;
    private javax.swing.JButton btnStart;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JList<String> listProcesos;
    private javax.swing.JPanel pnlMemoria;
    private javax.swing.JTextArea txtTablaProcesos;
    // End of variables declaration//GEN-END:variables
}
