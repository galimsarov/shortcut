package com.example.solid;

import com.example.solid.dip.DipExample;
import com.example.solid.isp.IspExample;
import com.example.solid.lsp.LspExample;
import com.example.solid.ocp.OcpExample;
import com.example.solid.srp.SrpExample;

public class SolidDemoApp {
    public static void main(String[] args) {
        System.out.println("=== SOLID definitions ===");
        for (SolidDefinitions.Definition definition : SolidDefinitions.all()) {
            System.out.println("- " + definition.name());
            System.out.println("  Canonical: " + definition.canonical());
            System.out.println("  Simplified: " + definition.simplified());
        }

        System.out.println("\n=== SRP ===");
        SrpExample.demo();

        System.out.println("\n=== OCP ===");
        OcpExample.demo();

        System.out.println("\n=== LSP ===");
        LspExample.demo();

        System.out.println("\n=== ISP ===");
        IspExample.demo();

        System.out.println("\n=== DIP ===");
        DipExample.demo();
    }
}
