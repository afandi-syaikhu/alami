package com.afandi.alami.delivery.cli;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;

import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@Component
public class EndOfDayRunner implements CommandLineRunner, ExitCodeGenerator {

    private final EndOfDayCommand eodCommand;

    private final IFactory factory;

    private int exitCode;

    public EndOfDayRunner(EndOfDayCommand eodCommand, IFactory factory) {
        this.eodCommand = eodCommand;
        this.factory = factory;
    }

    @Override
    public int getExitCode() {
        return this.exitCode;
    }

    @Override
    public void run(String... args) throws Exception {
        this.exitCode = new CommandLine(this.eodCommand, factory).execute(args);
    }
}
