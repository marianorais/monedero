package dds.monedero.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

public class Cuenta {

  private double saldo = 0;
  private List<Movimiento> movimientos = new ArrayList<>();

  public Cuenta() {
    this(0);
  }

  public Cuenta(double montoInicial) {
    saldo = montoInicial;
  }

  public void agregarMovimiento(Movimiento movimiento) {
    movimientos.add(movimiento);
  }

  public double getMontoExtraidoA(LocalDate fecha) {
    return getMovimientos().stream()
        .filter(movimiento -> !movimiento.isDeposito() && movimiento.getFecha().equals(fecha))
        .mapToDouble(Movimiento::getMonto)
        .sum();
  }

  public List<Movimiento> getMovimientos() {    
    return movimientos;
  }

  public double getSaldo() {
    return saldo;
  }

  public void poner(double cuanto) {
    validarMontoPositivo(cuanto);
    validarCantidadDepositosDiarios();
    new Deposito(LocalDate.now(), cuanto).agregateA(this);
  }

  public void sacar(double cuanto) {
    validarMontoPositivo(cuanto);
    validarMontoExtraccion(cuanto);
    validarLimite(cuanto);
    new Extraccion(LocalDate.now(), cuanto).agregateA(this);
  }

  public void setSaldo(double saldo) {
    this.saldo = saldo;
  }

  private void validarCantidadDepositosDiarios() {
    if (getMovimientos().stream().filter(movimiento -> movimiento.isDeposito()).count() >= 3) {
      throw new MaximaCantidadDepositosException("Ya excedio los " + 3 + " depositos diarios");
    }
  }

  private void validarLimite(double cuanto) {
    double limite = 1000 - getMontoExtraidoA(LocalDate.now());
    if (cuanto > limite) {
      throw new MaximoExtraccionDiarioException("No puede extraer mas de $ " + 1000
          + " diarios, límite: " + limite);
    }
  }

  private void validarMontoExtraccion(double cuanto) {
    if (getSaldo() - cuanto < 0) {
      throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
    }
  }

  private void validarMontoPositivo(double cuanto) {
    if (cuanto <= 0) {
      throw new MontoNegativoException(cuanto + ": el monto a ingresar debe ser un valor positivo");
    }
  }

}
