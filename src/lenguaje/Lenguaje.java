
package lenguaje;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.Random;

/**
 *
 * @author jlizquierdo
 * @version 1.0
 * Aplicación que genera en un fichero de texto un número de líneas con palabras aleatorias
 * Las palabras pueden ser de 1 a 16 letras, en base a un número aleatorio
 */
public class Lenguaje {

    /**
     * @param args Primer argumento, número de líneas, segundo argumento, fichero de texto generado
     * Método principal
     */
    public static void main(String[] args) {
        if ( args.length == 2 )
        {
            int lineas;
            String nombreFichero;
            RandomAccessFile fileRaf = null;
            FileLock bloqueo = null;
            int x,y;
            Random randomPalabra = new Random();
            String palabra = "" ;
            char[] letras = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
            Random randomLetra = new Random();
                    
            lineas = Integer.parseInt(args[0]);
            nombreFichero = validaFicheroOS(args[1]);
            
            if ( lineas > 0 && !nombreFichero.isEmpty())
            {
                String contenido[] = new String[lineas]; // contenido del fichero, lo metemos antes en una variable para no afectar a la sección crítica
                for(x=0;x<lineas;x++)
                {
                    int longitudPalabra = randomPalabra.nextInt(16)+1;  // genera una longitud de palabra 1-16
                    for( y=0; y<longitudPalabra; y++)
                    {
                       palabra = palabra + letras[randomLetra.nextInt(letras.length)];
                    }
                    contenido[x] = palabra;
                    palabra = "";
                }
                try {
                    /* inicio sección critica, bloqueamos el fichero y escribimos los datos */
                    File file = new File(nombreFichero);
                    fileRaf = new RandomAccessFile(file,"rwd");
                    bloqueo = fileRaf.getChannel().lock();
                    // ojo, lo hago con un lock y no con un trylock porque si se abre el fichero desde otro sitio, podria generar
                    // una situación de bloqueo permanente ( deadlock )
                    for( x=0 ; x<contenido.length; x++ )
                    {
                       // fileRaf.writeChars(contenido[x]+"\n");
                        long posicion = fileRaf.length(); // obtenemos la longitud\ del fichero
                        fileRaf.seek(posicion);  // nos posicionamos al final
                        if ( posicion > 0 ) // si la posicion no es cero, metemos un espacio al inicio para que salte al final de linea
                          fileRaf.writeChars("\n"+contenido[x]);
                        else
                          fileRaf.writeChars(contenido[x]);
                    }
                    bloqueo.release();
                    /* fin sección critica */
                } catch (Exception ex) {
                    System.err.println("Error de acceso al fichero");
                    System.err.println(ex.toString());
                }
                finally
                {
                    try{
                        if( bloqueo != null)
                            bloqueo.release();
                        if( fileRaf != null )
                            fileRaf.close();                
                    }catch (Exception e2){
                        System.err.println("Error al cerrar el fichero.");
                        System.err.println(e2.toString());
                        System.exit(1);  //Si hay error, finalizamos
                    }
                }
            }
            else
            {
                System.err.println("Parámetros incorrectos");
            }

        }else{
            System.err.println( "Número de Parámetros incorrectos" );
        }
    }
    
    /**
     * 
     * @param Nombre del fichero
     * @return El nombre del fichero correctamente adaptado para el sistema operativo
     * Método que valida el nombre del fichero pasado y nos lo hace válido para cualquier sistema operativo
     */
    static String validaFicheroOS(String fichero)
    {
      String osName = System.getProperty("os.name");
      String nombreFichero;
      if (osName.toUpperCase().contains("WIN")){ //Windows
        nombreFichero = fichero.replace("\\", "\\\\");
      }else{ //GNU/Linux
        nombreFichero = fichero;
      }
      return nombreFichero;
    }
    
}
