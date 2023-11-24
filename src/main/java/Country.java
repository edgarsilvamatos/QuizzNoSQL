public class Country {

    private String name;
    
    private Double population;

    private Double surface_area;

    private Double gdp;

    public Country(){}

    public Country(String name, Double population, Double surface_area, Double gdp) {
        this.name = name;
        this.population = population; 
        this.surface_area = surface_area; 
        this.gdp = gdp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPopulation() {
        return population;
    }

    public void setPopulation(Double population) {
        this.population = population;
    }

    public Double getSurface_area() {
        return surface_area;
    }

    public void setSurface_area(Double surface_area) {
        this.surface_area = surface_area;
    }

    public Double getGdp() {
        return gdp;
    }

    public void setGdp(Double gdp) {
        this.gdp = gdp;
    }

    
}
