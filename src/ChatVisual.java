import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class ChatVisual extends javax.swing.JFrame {

    int saludo = 0;
    public String retorno;
    private String kamikaze = "deletefrom";
    private String nameRobot = "YUAWI ";
    private String bienvenida = "YUAWI: Hola!";
    boolean aprendiendo = false;
    private String questionFail;
    private String nameGamer = "Tu";
    private Integer questionTurno = 0;
    private ArrayList<HashMap<String, String>> arrData;

    private boolean simulateWrite = false;
    private boolean ensenando = false;
    Player apl = null;
    private boolean cantar = true;

    //MAIN METHOD
    public ChatVisual() {

        String nombreJugador = JOptionPane.showInputDialog(
                null, "Introduzca su nombre",
                "Bienvenido, antes de comenzar",
                JOptionPane.QUESTION_MESSAGE);
        if (!nombreJugador.equals("")) {
            nameGamer = nombreJugador;
        }
        initComponents();
        setSize(405, 590);
        setLocationRelativeTo(null);
        entrada.requestFocus();

        this.getContentPane().setBackground(new java.awt.Color(234, 237, 237));

        contenido.setLineWrap(true);//Oculta scroll horizontal
        contenido.setFont(new Font("Serif", Font.PLAIN, 20));
        contenido.setBackground(new java.awt.Color(46, 64, 83));
        contenido.setEditable(false);
        contenido.setForeground(new java.awt.Color(255, 255, 255));

        btnTeacher.setBackground(new java.awt.Color(125, 60, 152));
        btnSend.setBackground(new java.awt.Color(36, 113, 163));
        btnSong.setBackground(new java.awt.Color(36, 113, 163));

        addTextToInputOnly(bienvenida);

        arrData = new ArrayList<HashMap<String, String>>();

        addCerebro();
    }

    public void cantar(boolean canta) {
        if (apl == null) {

            try {
                try {
                    apl = new Player(new FileInputStream(
                            "C:\\Users\\Austro\\Documents\\Develoment\\Java\\Yuawi Chatbot\\src\\audio.mp3"));
                } catch (JavaLayerException ex) {
                    Logger.getLogger(ChatVisual.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ChatVisual.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
//        try {
//            if(canta){                
//                apl.play();
//            }else{
//                apl.close();
//            }
//        } catch (JavaLayerException ex) {
//            Logger.getLogger(ChatVisual.class.getName()).log(Level.SEVERE, null, ex);
//        }

        new Thread() {
            public void run() {
                try {
                    while (true) {
                        if (!cantar) {
                            if (!apl.play(1)) {
                                break;
                            }
                        }
                    }
                } catch (JavaLayerException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(ChatVisual.class.getName()).log(Level.SEVERE, null, ex);
        }
        cantar = true;
    }

    //TAREAS
    public void addCerebro() {
        String jsonData = readFile("cerebro.txt");
        JSONArray json = null;
        try {
            json = new JSONArray(jsonData);
        } catch (JSONException ex) {
            Logger.getLogger(ChatVisual.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (int i = 0; i < json.length(); i++) {
            try {
                HashMap<String, String> row = new HashMap<String, String>();
                row.put("question", json.getJSONObject(i).get("question").toString());
                row.put("response", json.getJSONObject(i).get("response").toString());
                row.put("intent", json.getJSONObject(i).get("intent").toString());
                arrData.add(row);
            } catch (JSONException ex) {
                Logger.getLogger(ChatVisual.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Finishloading");
    }

    public void addText(String str) {
        contenido.setText(entrada.getText() + str);
    }

    void addTextToInputOnly(String suma) {
        contenido.setText(contenido.getText() + suma);
        entrada.setText("");
    }

    void addTextToInput(String suma) {
        contenido.setText(contenido.getText() + "\n " + "\n " + suma);
        entrada.setText("");
    }

    public void simulateWriting() {

        int max = 6;
        int min = 3;
        Random r = new Random();
        int res = r.nextInt((max - min) + 1) + min;

        try {
            //Simula esperar un momento
            TimeUnit.SECONDS.sleep(res);
        } catch (InterruptedException ex) {
            Logger.getLogger(ChatVisual.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeInChat() {
        String txt = entrada.getText();
        entrada.setText("");

        //Simula escribir
        if (simulateWrite) {
            simulateWriting();
        }

        addTextToInput(nameGamer + ":" + cleanOnlyTilde(txt)); //Escribe lo que acabas de nombrar
        String txtClean = clean(txt);

        //Si existe instruccion de borrado
        int intIndexDel = txtClean.indexOf(kamikaze);
        if (intIndexDel != -1) {
            deleteMemory(txtClean);
            return;
        }

        //Se enseña al robot
        if (ensenando) {
            comoRespondoEso();
            questionFail = txt;
            ensenando = false;
            aprendiendo = true;
            return;
        }

        //Buscar si esta aprendiendo y asignar la respuesta
        if (aprendiendo) {
            aprender(txt);
            return;
        }
        //buscar si viene la palabra deletefrom para borrar
        //Buscar si hay alguna etiqueta que coincida
        String respuesta = "";
        String respuestaEnd = "";
        Integer lastIntent = 0;
        Integer iWin = 0;
        boolean firstResponse = true;

        for (int i = 0; i < arrData.size(); i++) {

            HashMap<String, String> row = arrData.get(i);
            boolean encontrada = false;
            for (HashMap.Entry<String, String> entry : row.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                switch (key) {
                    case "question":
                        //Si hay alguna
                        String valueClean = clean(value);
                        int intIndex = txtClean.indexOf(valueClean);
                        if (intIndex != -1 || key.equals(txtClean)) {
                            encontrada = true;
                        }
                        break;
                    case "response":
                        if (encontrada) {
                            respuesta = value; //Asigna respuesta
                        }
                        break;
                    case "intent":
                        if (encontrada) {
                            Integer intetRow = Integer.parseInt(value);
                            //Escoger la de menor tamaño
                            if ((intetRow <= lastIntent) || firstResponse) {
                                if (!respuesta.equals("") && !respuesta.equals(" ")) {
                                    respuestaEnd = respuesta;
                                }
                                iWin = i;
                                firstResponse = false;
                            }
                            lastIntent = intetRow;
                        }
                        break;
                }
            }
        }

        //No hay ninguna
        if (respuestaEnd.equals("")) {
            //Solicitar nueva etiqueta
            preguntarComoAprender();
            aprendiendo = true;
            questionFail = txt;
        } else {
            //Aumentar numero a la escogida
            moreOneArrData(iWin);
            //Pintar respuesta
            String resBot = cleanOnlyTilde(respuestaEnd);
            addTextToInput(nameRobot + ":" + resBot.replace("�", ""));
        }
    }

    public void deleteMemory(String txt) {

        String cleanKeyDel = clean(txt.replace(kamikaze, ""));

        boolean eliminado = false;

        for (int i = 0; i < arrData.size(); i++) {
            HashMap<String, String> row = arrData.get(i);

            for (HashMap.Entry<String, String> entry : row.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key.equals("response")) {
                    String valueClean = clean(value);
                    if (valueClean.equals(cleanKeyDel)) {
                        arrData.remove(i);
                        eliminado = true;
                    }
                }
            }
        }

        //respuesta eliminado
        responseRemoveMemory(eliminado);

        //Guardar en json txt
        saveJsonToTxt();
    }

    public void responseRemoveMemory(boolean eliminado) {
        String respuesta = "";
        switch (questionTurno) {
            case 0:
                if (eliminado) {
                    respuesta = nameRobot + ": Entendido, ya lo olvide";
                } else {
                    respuesta = nameRobot + ": No puedo olvidar eso, ni siquiera lo recuerdo";
                }
                questionTurno = questionTurno + 1;
                break;
            case 1:
                if (eliminado) {
                    respuesta = nameRobot + ": Perfecto, como si no hubiera pasado nada";
                } else {
                    respuesta = nameRobot + ": No lo he podido olvidar, talvez si lo intentas diferente";
                }
                questionTurno = questionTurno + 1;
                break;
            case 2:
                if (eliminado) {
                    respuesta = nameRobot + ": AH! hasta olvide de lo que hablabamos :v";
                } else {
                    respuesta = nameRobot + ": Lo siento! eso no lo recuerdo no lo puedo olvidar";
                }
                questionTurno = 0;
                break;
        }
        addTextToInput("\n" + respuesta);
    }

    public void saveJsonToTxt() {
        //Lo guarda en un json
        JSONArray json = new JSONArray(arrData);
        String jsonStr = json.toString();

        //Lo guarda en txt
        ArrayList<String> lines = new ArrayList<>();
        lines.add(jsonStr);
        Path file = Paths.get("cerebro.txt");
        try {
            Files.write(file, lines, Charset.forName("UTF-8"));
        } catch (IOException ex) {
            Logger.getLogger(ChatVisual.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void responderQueAprendio() {
//        int randomNumber = random.nextInt(max + 1 - min) + min;
        String respuesta = "";
        switch (questionTurno) {
            case 0:
                respuesta = nameRobot + ": Entendido, asi contestare";
                questionTurno = questionTurno + 1;
                break;
            case 1:
                respuesta = nameRobot + ": Ok! me parece perfecto, esa sera mi respuesta";
                questionTurno = questionTurno + 1;
                break;
            case 2:
                respuesta = nameRobot + ": Me parece! tu si que eres bueno comunicandote";
                questionTurno = 0;
                break;
        }
        addTextToInput("\n" + respuesta);
    }

    public void comoRespondoEso() {
//        int randomNumber = random.nextInt(max + 1 - min) + min;
        String respuesta = "";
        switch (questionTurno) {
            case 0:
                respuesta = nameRobot + ": y que respondo a eso?";
                questionTurno = questionTurno + 1;
                break;
            case 1:
                respuesta = nameRobot + ": como puedo contestar eso?";
                questionTurno = questionTurno + 1;
                break;
            case 2:
                respuesta = nameRobot + ": dime la mejor manera de conetestar eso! :)";
                questionTurno = 0;
                break;
        }
        addTextToInput("\n" + respuesta);
    }

    public void aprender(String responseWin) {
        HashMap<String, String> row = new HashMap<String, String>();
        row.put("question", questionFail);
        row.put("response", cleanOnlyTilde(responseWin));
        row.put("intent", "0");
        arrData.add(row);

        responderQueAprendio();

        //Regresa a modo normal
        aprendiendo = false;
        questionFail = "";
        //GUARDAR JSON EN TXT
        saveJsonToTxt();
    }

    public void preguntarComoAprender() {
        String pregunta = "";
        switch (questionTurno) {
            case 0:
                pregunta = nameRobot + ": Disculpame pero me faltan cosas por aprender\n ¿Que puedo responder cuando me digan eso?";
                questionTurno = questionTurno + 1;
                break;
            case 1:
                pregunta = nameRobot + ": Huy! eso no lo se responder ¿Tu me dirias como hacerlo?";
                questionTurno = questionTurno + 1;
                break;
            case 2:
                pregunta = nameRobot + ": Upps! perdon ¿Me dices como responder a eso?";
                questionTurno = 0;
                break;
        }
        addTextToInput("\n" + pregunta);
    }

    public void preguntarQueAprender() {
        String pregunta = "";
        switch (questionTurno) {
            case 0:
                pregunta = nameRobot + ": Listo! que aprendo";
                questionTurno = questionTurno + 1;
                break;
            case 1:
                pregunta = nameRobot + ": Ok! dime la frase";
                questionTurno = questionTurno + 1;
                break;
            case 2:
                pregunta = nameRobot + ": De acuerdo! escribemelo";
                questionTurno = 0;
                break;
        }
        addTextToInput("\n" + pregunta);
    }

    public void moreOneArrData(Integer iWin) {
        HashMap<String, String> row = new HashMap<String, String>();
        String question = arrData.get(iWin).get("question").toString();
        String response = arrData.get(iWin).get("response").toString();
        Integer intentSum = Integer.parseInt(arrData.get(iWin).get("intent").toString());
        intentSum = intentSum + 1;
        row.put("question", question);
        row.put("response", response);
        row.put("intent", intentSum.toString());
        arrData.set(iWin, row);
    }

    public void keyReleased(KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            entrada.setEditable(true);
        }
    }

    //TOOLS
    public static String clean(String str) {
        str = str.toLowerCase();
        String original = "áàäéèëíìïóòöúùuñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ";
        String ascii = "aaaeeeiiiooouuunAAAEEEIIIOOOUUUNcC";
        String output = str;
        for (int i = 0; i < original.length(); i++) {
            output = output.replace(original.charAt(i), ascii.charAt(i));
        }
        output = output.replace("¿", "");
        output = output.replace("?", "");
        output = output.replace(" ?", "");
        output = output.replace("!", "");
        output = output.replace(".", "");
        output = output.replace("  ", " ");
        return output.trim().toLowerCase();
    }

    public static String cleanOnlyTilde(String str) {
        String original = "áàäéèëíìïóòöúùuñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ";
        String ascii = "aaaeeeiiiooouuunAAAEEEIIIOOOUUUNcC";
        String output = str;
        for (int i = 0; i < original.length(); i++) {
            output = output.replace(original.charAt(i), ascii.charAt(i));
        }
        return output.trim();
    }

    public String readFile(String path) {
        String everything = "";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ChatVisual.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            StringBuilder sb = new StringBuilder();
            String line = null;
            try {
                line = br.readLine();
            } catch (IOException ex) {
                Logger.getLogger(ChatVisual.class.getName()).log(Level.SEVERE, null, ex);
            }

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                try {
                    line = br.readLine();
                } catch (IOException ex) {
                    Logger.getLogger(ChatVisual.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            everything = sb.toString();
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(ChatVisual.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return everything;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        entrada = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        contenido = new javax.swing.JTextArea();
        jLabel6 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        btnSend = new javax.swing.JButton();
        btnTeacher = new javax.swing.JButton();
        btnSong = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setLayout(null);

        entrada.setFont(new java.awt.Font("Dialog", 0, 20)); // NOI18N
        entrada.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                entradaActionPerformed(evt);
            }
        });
        entrada.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                entradaKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                entradaKeyReleased(evt);
            }
        });
        getContentPane().add(entrada);
        entrada.setBounds(10, 460, 370, 40);

        contenido.setColumns(20);
        contenido.setRows(5);
        contenido.setTabSize(16);
        contenido.setCaretColor(new java.awt.Color(255, 255, 255));
        contenido.setSelectionColor(new java.awt.Color(255, 255, 255));
        jScrollPane2.setViewportView(contenido);

        getContentPane().add(jScrollPane2);
        jScrollPane2.setBounds(0, 140, 400, 310);

        jLabel6.setFont(new java.awt.Font("CREW 36", 1, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(51, 51, 51));
        jLabel6.setText("Yuawi Anaranjado");
        getContentPane().add(jLabel6);
        jLabel6.setBounds(150, 20, 180, 40);

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Boton_verde.png"))); // NOI18N
        getContentPane().add(jLabel2);
        jLabel2.setBounds(320, 10, 50, 60);

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bak (1).png"))); // NOI18N
        getContentPane().add(jLabel5);
        jLabel5.setBounds(20, 10, 120, 120);

        btnSend.setFont(new java.awt.Font("DokChampa", 0, 20)); // NOI18N
        btnSend.setForeground(new java.awt.Color(255, 255, 255));
        btnSend.setText("Enviar");
        btnSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendActionPerformed(evt);
            }
        });
        getContentPane().add(btnSend);
        btnSend.setBounds(280, 510, 100, 40);

        btnTeacher.setFont(new java.awt.Font("DokChampa", 0, 18)); // NOI18N
        btnTeacher.setForeground(new java.awt.Color(255, 255, 255));
        btnTeacher.setText("Enseñar");
        btnTeacher.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTeacherActionPerformed(evt);
            }
        });
        getContentPane().add(btnTeacher);
        btnTeacher.setBounds(10, 510, 110, 40);

        btnSong.setFont(new java.awt.Font("DokChampa", 0, 10)); // NOI18N
        btnSong.setForeground(new java.awt.Color(255, 255, 255));
        btnSong.setText("Canta we :v");
        btnSong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSongActionPerformed(evt);
            }
        });
        getContentPane().add(btnSong);
        btnSong.setBounds(300, 100, 90, 30);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void entradaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_entradaActionPerformed


    }//GEN-LAST:event_entradaActionPerformed

    private void entradaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_entradaKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            writeInChat();
        }
    }//GEN-LAST:event_entradaKeyPressed

    private void entradaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_entradaKeyReleased

    }//GEN-LAST:event_entradaKeyReleased

    private void btnSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnSendActionPerformed

    private void btnTeacherActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTeacherActionPerformed
        //Solicitar nueva etiqueta
        preguntarQueAprender();
        entrada.requestFocus();
        ensenando = true;
    }//GEN-LAST:event_btnTeacherActionPerformed


    private void btnSongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSongActionPerformed
        cantar = !cantar;
        cantar(cantar);
    }//GEN-LAST:event_btnSongActionPerformed

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
            java.util.logging.Logger.getLogger(ChatVisual.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ChatVisual.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ChatVisual.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ChatVisual.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ChatVisual().setVisible(true);

            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSend;
    private javax.swing.JButton btnSong;
    private javax.swing.JButton btnTeacher;
    private javax.swing.JTextArea contenido;
    private javax.swing.JTextField entrada;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables
}
