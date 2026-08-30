package com.saveur221.entities;

import java.time.LocalDateTime;

public class Avis {

    private Integer id;
    private int note;
    private String commentaire;
    private LocalDateTime dateAvis;
    private Integer clientId;
    private Integer commandeId;

    public Avis() {
    }

    public Avis(Integer id, int note, String commentaire, LocalDateTime dateAvis,
                Integer clientId, Integer commandeId) {
        this.id = id;
        this.note = note;
        this.commentaire = commentaire;
        this.dateAvis = dateAvis;
        this.clientId = clientId;
        this.commandeId = commandeId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public LocalDateTime getDateAvis() {
        return dateAvis;
    }

    public void setDateAvis(LocalDateTime dateAvis) {
        this.dateAvis = dateAvis;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public Integer getCommandeId() {
        return commandeId;
    }

    public void setCommandeId(Integer commandeId) {
        this.commandeId = commandeId;
    }

    @Override
    public String toString() {
        return String.format("%d/5 - %s", note, commentaire);
    }
}
