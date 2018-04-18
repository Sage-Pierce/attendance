package attendance;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import commonstuff.AlignedPanel;
import commonstuff.Helper;

public class Attendance extends JPanel implements ActionListener, KeyListener
{
   private static final String EXCUSED = "E";
   private static final String UNEXCUSED = "U";
   private static final String PRESENT = "P";
   private static final int NUM_COLS = 4;

   private BufferedReader input;
   private BufferedWriter output;
   private int count, numPeople;
   private JButton submit, finish;
   private JLabel submissionStatus;
   private JPanel dataPanel, submissionPanel, southPanel, statusPanel;
   private AlignedPanel[] firstNames, lastNames, status;
   private JTextField entry;
   private Vector<Person> people;
   private Vector<String> entries;
   
   public Attendance() throws IOException
   {
      super(new BorderLayout());
      setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
      
      File inFile = Helper.promptForFile();
      if (inFile == null)
    	 System.exit(0);
      
      String outPath = inFile.getParent();
      outPath = outPath.concat(System.getProperty("file.separator"));
      outPath = outPath.concat("Output.txt");
     
      input = new BufferedReader(new FileReader(inFile));
      output = new BufferedWriter(new FileWriter(new File(outPath)));

      dataPanel = new JPanel();
      dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.X_AXIS));
 
      firstNames = new AlignedPanel[NUM_COLS];
      lastNames = new AlignedPanel[NUM_COLS];
      status = new AlignedPanel[NUM_COLS];
      for (int i = 0; i < NUM_COLS; i++)
      {
    	  firstNames[i] = new AlignedPanel("RIGHT");
    	  lastNames[i] = new AlignedPanel("RIGHT");
    	  status[i] = new AlignedPanel("CENTER");
    	  
    	  firstNames[i].add(new JLabel("First"), "CENTER");
    	  lastNames[i].add(new JLabel("Last"), "CENTER");
    	  status[i].add(new JLabel("Status"), "CENTER");
      }
      
      String line;
      entries = new Vector<String>();
      while ((line = input.readLine()) != null)
    	 entries.add(line);
      
      count = 0;
      numPeople = entries.size();
      people = new Vector<Person>();
      for (String entry : entries)
      {
         Person newPerson = new Person(entry.split("\\s+"));
         people.add(newPerson);
      }
      
      for (int i = 0; i < NUM_COLS; i++)
      {
    	  lastNames[i].complete();
    	  firstNames[i].complete();
    	  status[i].complete();
    	  
          dataPanel.add(lastNames[i]);
          dataPanel.add(Box.createHorizontalStrut(5));
          dataPanel.add(firstNames[i]);
          dataPanel.add(Box.createHorizontalStrut(5));
          dataPanel.add(status[i]);
          
          if (i < (NUM_COLS - 1))
            dataPanel.add(Box.createHorizontalStrut(10));
      }
      
      southPanel = new JPanel();
      southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
      
      submissionPanel = new JPanel();
      submissionPanel.setLayout(new BoxLayout(submissionPanel, BoxLayout.X_AXIS));

      entry = new JTextField();
      entry.addKeyListener(this);
      submit = new JButton("Submit");
      submit.setFocusable(false);
      submit.addActionListener(this);
      submissionPanel.add(entry);
      submissionPanel.add(Box.createHorizontalStrut(5));
      submissionPanel.add(submit);
      
      statusPanel = new JPanel();
      statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
      
      submissionStatus = new JLabel("No Submission Yet");
      finish = new JButton("Save and Quit");
      finish.setFocusable(false);
      finish.addActionListener(this);
      statusPanel.add(submissionStatus);
      statusPanel.add(Box.createHorizontalGlue());
      statusPanel.add(finish);
      
      JPanel key = new JPanel();
      key.setLayout(new BoxLayout(key, BoxLayout.X_AXIS));
      key.setBorder(BorderFactory.createTitledBorder("Key"));
      key.add(new JLabel("U = UNEXCUSED ABSENCE  |  P = PRESENT  |  E = EXCUSED ABSENCE"));
      
      AlignedPanel instructionPanel = new AlignedPanel("LEFT");
      instructionPanel.setBorder(BorderFactory.createTitledBorder("Instructions"));
      instructionPanel.add(new JLabel("Please enter UNIQUE first OR last name. " +
                                      "If neither is unique or not found, try both."));
      instructionPanel.add(new JLabel("If neither of those works, find your name " +
                                      "and click on it."));
      
      southPanel.add(instructionPanel);
      southPanel.add(submissionPanel);
      southPanel.add(Box.createVerticalStrut(3));
      //southPanel.add(key);
      southPanel.add(Box.createVerticalStrut(8));
      southPanel.add(statusPanel);
      
      add(dataPanel, BorderLayout.NORTH);
      add(key, BorderLayout.CENTER);
      add(southPanel, BorderLayout.SOUTH);
   }
 
   private void processSubmit()
   {
      boolean duplicate = false;
      int index = -1, temp, closest = -1;
      
      for (int i = 0; i < people.size(); i++)
      {
         Person p = people.get(i);
         temp = p.compare(entry.getText().split(" "));
         
         if ((closest == -1) || (temp < closest))
         {
            index = i;
            closest = temp;
            duplicate = false;
         }
         else if (temp == closest)
         {
            duplicate = true;
         }
      }
//System.out.println("Closest: " + closest);
      if (duplicate)
      {
         java.awt.Toolkit.getDefaultToolkit().beep();
         submissionStatus.setText("Error: found > 1 match");
      }
      else if (index != -1)
      {
         people.get(index).affect();
         submissionStatus.setText("Submitted: " + people.get(index).getName());
      }
      else
      {
         submissionStatus.setText("Unknown Error");
      }

      entry.setText("");
   }
   
   private void finish()
   {
      try
      {
         for (int i = 0; i < people.size(); i++)
         {
            Person p = people.get(i);
            output.write(p.getStatus());
            output.newLine();
         }

         input.close();
         output.close();
         System.exit(0);
      }
      catch (Exception e) {}
   }
   
   @Override
    public void keyTyped(KeyEvent e) {}
   @Override
    public void keyReleased(KeyEvent e) {} 
   @Override
   public void keyPressed(KeyEvent e) 
   {
      if ((e.getKeyCode() == KeyEvent.VK_ENTER) &&
          (e.getSource() == entry))
         processSubmit();
   }
   @Override
   public void actionPerformed(ActionEvent e)
   {
      if (e.getSource() == submit) 
         processSubmit();
      else if (e.getSource() == finish)
         finish();
   }
   
   private class Person implements MouseListener
   {
      private boolean valid = false;
      private JLabel first, last, stat;
      
      private Person (String[] data)
      {
         if ((data.length < 2) || (data.length > 3))
         {
            System.out.println("Error: erroneous String data");
            return;
         }
         
         if (data.length == 3)
            stat = new JLabel(data[2]);
         else
            stat = new JLabel(UNEXCUSED);
         
         last = new JLabel(data[0].toLowerCase());
         first = new JLabel(data[1].toLowerCase());
         
         int index = count / ((numPeople / NUM_COLS) + 1);
         lastNames[index].add(last);
         firstNames[index].add(first);
         status[index].add(stat);
         
         first.addMouseListener(this);
         last.addMouseListener(this);
         stat.addMouseListener(this);
         
         count++;
         valid = true;
      }
      
      private int compare(String[] name)
      {
         if (name.length < 1)
            return -1;
         
         String lcFirst = name[0].toLowerCase();
         String shortFirst;
         if (first.getText().length() > lcFirst.length())
            shortFirst = first.getText().substring(0, lcFirst.length());
         else
            shortFirst = first.getText();
         
            // if only one name provided, check for exact match to either name
         if(name.length == 1)
         {
            int primary = Math.min(numDiff(lcFirst, first.getText()),
                                   numDiff(lcFirst, last.getText()));
               // Check shortened name 
            if (primary != 0)
            {
               int secondary;
               secondary = numDiff(lcFirst, shortFirst);
               return Math.min(primary, secondary);
            }
            else
               return primary;
         }         
         else
         {
            String lcLast = name[1].toLowerCase(); 
            int primary = numDiff(lcFirst, first.getText()) +
                          numDiff(lcLast, last.getText());

            if (primary != 0)
            {
               int secondary;
               secondary = numDiff(shortFirst, first.getText()) +
                           numDiff(lcLast, last.getText());
               return Math.min(primary, secondary);
            }
            else
               return primary;
         }
      }
      
      private void affect()
      {
         if (stat.getText().equals(UNEXCUSED))
            stat.setText(PRESENT);
         else if (stat.getText().equals(PRESENT))
         {
            java.awt.Toolkit.getDefaultToolkit().beep();
            stat.setText(EXCUSED);
         }
         else // must be EXCUSED
         {
            java.awt.Toolkit.getDefaultToolkit().beep();
            stat.setText(UNEXCUSED);
         }
      }
      
      private int numDiff(String one, String two)
      {
         int i, sum;
         String longer, shorter;
         int[] first = new int[26];
         int[] second = new int[26];
         
         sum = 0;
         if (one.length() > two.length())
         {
            longer = one;
            shorter = two;
         }
         else
         {
            longer = two;
            shorter = one;
         }
         
         for (i = 0; i < longer.length(); i++)
         {
        	if (!Character.isLetter(longer.charAt(i)))
        	   continue;
        	
            first[longer.charAt(i) - 'a']++;
            
            if ((i < shorter.length()) && Character.isLetter(shorter.charAt(i)))
              second[shorter.charAt(i) - 'a']++;
         }
         
         for (i = 0; i < 26; i++)
            sum += Math.abs(first[i] - second[i]);

         return sum;
      }

      public void mouseClicked(MouseEvent e)
      {
         affect();
      } 
      
      public void mousePressed(MouseEvent e) {}
      public void mouseReleased(MouseEvent e) {}
      public void mouseEntered(MouseEvent e) {}
      public void mouseExited(MouseEvent e) {}     

      private void setStatus(String s) { stat.setText(s); }
      private boolean isValid() { return valid; }
      private String getName() {return first.getText() + " " + last.getText();}
      private String getStatus() { return stat.getText(); }
   }
   
   public static void main(String[] args)
   {
      try
      {
         Attendance content = new Attendance();
      
         JFrame f = new JFrame("Attendance");
         f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         f.getContentPane().add(content);
         f.pack();
         f.setLocation(30, 20);
         f.setVisible(true);
      }
      catch (Exception e) {}
   }
}
