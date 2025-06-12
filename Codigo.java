import java.util.*;

class Paciente implements Comparable<Paciente>{
    private String nombre;
    private String apellido;
    private String id;
    private int categoria;
    private long tiempoLlegada;
    private String estado;
    private String area;
    private Stack<String> historialCambios;

    public Paciente(String nombre, String apellido, String id, int categoria, long tiempoLlegada, String area) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.id = id;
        this.categoria = categoria;
        this.tiempoLlegada = tiempoLlegada;
        this.estado = "en_espera";
        this.area = area;
        this.historialCambios = new Stack<>();
    }
    @Override
    public int compareTo(Paciente otro) {
        // Ordenar por categoría (mayor prioridad = número más bajo)
        // Por ejemplo: C1 (más urgente) > C5 (menos urgente)
        return Integer.compare(this.categoria, otro.categoria);
    }

    public long tiempoEsperaActual(long tiempoActual) {
        return (tiempoActual - this.tiempoLlegada) / 60;
    }

    public void registrarCambio(String descripcion) {
        historialCambios.push(descripcion);
    }

    public String obtenerUltimoCambio() {
        return historialCambios.isEmpty() ? "Sin cambios" : historialCambios.pop();
    }

    //no se si son necesario los getters y setters
    public int getCategoria() {
        return categoria;
    }

    public String getId() {
        return id;
    }

    public long getTiempoLlegada() {
        return tiempoLlegada;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String nuevoEstado) {
        this.estado = nuevoEstado;
    }

    public String getArea() {
        return area;
    }

    public void setCategoria(int nuevaCategoria) {
        this.categoria = nuevaCategoria;
    }
}

class AreaAtencion {
    private String nombreArea;
    private PriorityQueue<Paciente> pacientesHeap;
    private int capacidadMaxima;

    public AreaAtencion(String nombreArea, int capacidadMaxima) {
        this.nombreArea = nombreArea;
        this.capacidadMaxima = capacidadMaxima;
        this.pacientesHeap = new PriorityQueue<>((p1, p2) ->
                Integer.compare(p2.getCategoria(), p1.getCategoria()));

    }

    public void ingresarPaciente(Paciente p) {
        if (!estaSaturada()) {
            pacientesHeap.add(p);
        }
    }

    public Paciente atenderPaciente() {
        return pacientesHeap.poll();
    }

    public boolean estaSaturada() {
        return pacientesHeap.size() >= capacidadMaxima;
    }
    public void atenderPaciente(Paciente p) {
        pacientesHeap.remove(p);
    }

    public void actualizarPaciente(Paciente p) {
        if (pacientesHeap.remove(p)) {
            pacientesHeap.add(p);
        }
    }



    public String getNombre() {
        return nombreArea;
    }
}

class Hospital{
    private Map<String, Paciente> pacientesTotales;
    private PriorityQueue<Paciente> colaAtencion;
    private Map<String, AreaAtencion> areasAtencion;
    private List<Paciente> pacientesAtendidos;

    public Hospital(){
        this.pacientesTotales=new HashMap<>();
        this.colaAtencion=new PriorityQueue<>();
        this.areasAtencion=new HashMap<>();
        this.pacientesAtendidos=new ArrayList<>();
    }

    public int getTotalPacientesAtendidos() {
        return pacientesAtendidos.size();
    }

    public void registrarPaciente(Paciente p){
        pacientesTotales.put(p.getId(),p);
        colaAtencion.offer(p);
    }

    public void reasignarCategoria(String id,int nuevaCategoria){
        Paciente p =pacientesTotales.get(id);
        if (p !=null){
            p.setCategoria(nuevaCategoria);
            p.registrarCambio("Categoría reasignada a "+nuevaCategoria);
        }
    }

    public Paciente atenderSiguiente(){
        Paciente siguiente=colaAtencion.poll();
        if (siguiente!=null) {
            siguiente.setEstado("atendido");
            pacientesAtendidos.add(siguiente);
        }
        return siguiente;
    }

    public List<Paciente> obtenerPacientesPorCategoria(int categoria){
        List<Paciente> lista = new ArrayList<>();
        for (Paciente p : colaAtencion){
            if (p.getCategoria() ==categoria){
                lista.add(p);
            }
        }
        return lista;
    }

    public AreaAtencion obtenerArea(String nombre){
        return areasAtencion.get(nombre);
    }

    public void agregarArea(AreaAtencion area) {
        areasAtencion.put(area.getNombre(), area);
    }
    public PriorityQueue<Paciente> getColaAtencion() {
        return colaAtencion;
    }
    public List<Paciente> getPacientesAtendidos() {
        return pacientesAtendidos;
    }

}



class GeneradorPacientes {
    private static final String[] nombres = {"Juan", "Ana", "Luis", "Pedro", "Camila"};
    private static final String[] apellidos = {"Pérez", "Soto", "González", "Rojas", "López"};
    private static final String[] areas = {"SAPU", "urgencia_adulto", "infantil"};
    private static final Random rand = new Random();

    public static List<Paciente> generarPacientes(int cantidad, long timestampInicio) {
        List<Paciente> lista = new ArrayList<>();

        for (int i = 0; i < cantidad; i++) {
            String nombre = nombres[rand.nextInt(nombres.length)];
            String apellido = apellidos[rand.nextInt(apellidos.length)];
            String id = "P" + (1000 + i);
            int categoria = generarCategoria();
            long llegada = timestampInicio + (i * 600);
            String area = areas[rand.nextInt(areas.length)];

            Paciente p = new Paciente(nombre, apellido, id, categoria, llegada, area);
            lista.add(p);
        }

        return lista;
    }

    private static int generarCategoria() {
        int prob = rand.nextInt(100);
        if (prob < 10) return 1;
        if (prob < 25) return 2;
        if (prob < 43) return 3;
        if (prob < 70) return 4;
        return 5;
    }
}

class SimuladorUrgencia {
    private Hospital hospital;
    private List<Paciente> pacientes;
    private int pacientesAgregados = 0;
    private Map<Integer, List<Long>> tiemposPorCategoria = new HashMap<>();
    private Map<Integer, Long> peorTiempoPorCategoria = new HashMap<>();
    private List<Paciente> fueraDeTiempo = new ArrayList<>();
    private long tiempoActual;

    public SimuladorUrgencia(Hospital hospital, List<Paciente> pacientes) {
        this.hospital = hospital;
        this.pacientes = pacientes;
    }

    public void simular(int pacientesPorDia) {
        tiempoActual = pacientes.get(0).getTiempoLlegada();
        int pacientesEnCola = 0;

        for (int minuto = 0; minuto < 1440; minuto++) {
            tiempoActual += 60;

            if (minuto % 10 == 0 && pacientesAgregados < pacientesPorDia) {
                Paciente nuevo = pacientes.get(pacientesAgregados++);
                hospital.registrarPaciente(nuevo);
                pacientesEnCola++;
            }

            if (minuto % 15 == 0 && !hospital.getColaAtencion().isEmpty()) {
                atenderYRegistrar(tiempoActual);
                pacientesEnCola = Math.max(0, pacientesEnCola - 1);
            }

            if (pacientesEnCola >= 3 && !hospital.getColaAtencion().isEmpty()) {
                atenderYRegistrar(tiempoActual);
                atenderYRegistrar(tiempoActual);
                pacientesEnCola = Math.max(0, pacientesEnCola - 2);
            }
        }
    }

    private void atenderYRegistrar(long tiempoActual) {
        Paciente p = hospital.atenderSiguiente();
        if (p != null) {
            long espera = (tiempoActual - p.getTiempoLlegada()) / 60;
            int cat = p.getCategoria();

            tiemposPorCategoria.computeIfAbsent(cat, k -> new ArrayList<>()).add(espera);

            peorTiempoPorCategoria.put(cat,
                    Math.max(peorTiempoPorCategoria.getOrDefault(cat, 0L), espera));

            if (excedeLimite(cat, espera)) {
                fueraDeTiempo.add(p);
            }
        }
    }

    private boolean excedeLimite(int cat, long espera) {
        return switch (cat) {
            case 1 -> espera > 0;
            case 2 -> espera > 30;
            case 3 -> espera > 90;
            case 4 -> espera > 180;
            default -> false; // C5 no tiene límite
        };
    }

    public void imprimirResultados() {
        System.out.println("\n--- Resultados por categoría ---");
        for (int cat = 1; cat <= 5; cat++) {
            List<Long> tiempos = tiemposPorCategoria.getOrDefault(cat, new ArrayList<>());
            long suma = tiempos.stream().mapToLong(Long::longValue).sum();
            double promedio = tiempos.isEmpty() ? 0 : (double) suma / tiempos.size();
            long peor = peorTiempoPorCategoria.getOrDefault(cat, 0L);

            System.out.printf("C%d -> Promedio: %.2f min | Peor: %d min | Atendidos: %d\n",
                    cat, promedio, peor, tiempos.size());
        }

        System.out.println("\nPacientes que excedieron tiempo máximo:");
        for (Paciente p : fueraDeTiempo) {
            long espera = (tiempoActual - p.getTiempoLlegada()) / 60;
            System.out.printf("%s (C%d): %d min de espera\n", p.getId(), p.getCategoria(), espera);
        }
    }
}


class HeapSortHospital {
    public static List<Paciente> ordenarPacientes(List<Paciente> pacientes) {
        PriorityQueue<Paciente> heap = new PriorityQueue<>(pacientes);
        List<Paciente> resultado = new ArrayList<>();
        while (!heap.isEmpty()) {
            resultado.add(heap.poll());
        }
        return resultado;
    }
}

public class Main {
    public static void main(String[] args) {
        long timestampInicio = 0L; // Simulación desde 00:00 (timestamp base)
        int pacientesPorDia = 144;

        // Crear hospital y generar pacientes
        Hospital hospital = new Hospital();
        List<Paciente> pacientes = GeneradorPacientes.generarPacientes(pacientesPorDia, timestampInicio);

        // Ejecutar simulación
        SimuladorUrgencia simulador = new SimuladorUrgencia(hospital, pacientes);
        simulador.simular(pacientesPorDia);

        // Resultados básicos
        System.out.println("Pacientes atendidos: " + hospital.getPacientesAtendidos().size());

        // Ejemplo de análisis por categoría
        for (int cat = 1; cat <= 5; cat++) {
            List<Paciente> enEspera = hospital.obtenerPacientesPorCategoria(cat);
            System.out.println("Pacientes en espera categoría C" + cat + ": " + enEspera.size());
        }
    }
}
