/*
 * Copyright (c) 2002-2009, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.actes.modules.s2low.service;

import fr.gouv.interieur.actes_v1.FichierSigne;

import fr.paris.lutece.plugins.actes.business.Acte;
import fr.paris.lutece.plugins.actes.business.transaction.Transaction;
import fr.paris.lutece.plugins.actes.service.ITransmissionService;
import fr.paris.lutece.plugins.actes.service.TransmissionException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * S2LOW implementation
 */
public class S2lowService implements ITransmissionService
{
    private static final String URL_CREATE = "/modules/actes/actes_transac_create.php";
    private static final String PROPERTY_S2LOW_SERVER = "actes-s2low.server";
    private static final String PROPERTY_S2LOW_PORT = "actes-s2low.port";
    private static final String PROPERTY_S2LOW_KEYSTORE_PATH = "actes-s2low.keystore.path";
    private static final String PROPERTY_S2LOW_KEYSTORE_PASSWORD = "actes-s2low.keystore.password";
    private static final String PROPERTY_S2LOW_KEYSTORE_TYPE = "actes-s2low.keystore.type";
    private static final String PROPERTY_S2LOW_KEYTRUST_PATH = "actes-s2low.keytrust.path";
    private static final String PROPERTY_S2LOW_KEYTRUST_PASSWORD = "actes-s2low.keytrust.password";
    private static final String PROPERTY_S2LOW_KEYTRUST_TYPE = "actes-s2low.keytrust.type";
    private static final String PROPERTY_S2LOW_PROXY_HOST = "actes-s2low.proxy.host";
    private static final String PROPERTY_S2LOW_PROXY_PORT = "actes-s2low.proxy.port";
    private static final String PROPERTY_S2LOW_PROXY_USERNAME = "actes-s2low.proxy.username";
    private static final String PROPERTY_S2LOW_PROXY_PASSWORD = "actes-s2low.proxy.password";
    private static final String PROPERTY_S2LOW_HOSTNAME = "actes-s2low.hostname";
    private static final String PROPERTY_S2LOW_DOMAIN = "actes-s2low.domain";
    private static final String PROPERTY_S2LOW_REALM = "actes-s2low.realm";
    private static final String DEFAULT_STORE_TYPE = "JKS";
    private String _strServerAddress;
    private String _strPort;
    private String _strKeyStorePath;
    private String _strKeyStorePassword;
    private String _strKeyStoreType;
    private String _strKeyTrustPath;
    private String _strKeyTrustPassword;
    private String _strKeyTrustType;
    private String _strProxyHost;
    private String _strProxyPort;
    private String _strProxyUserName;
    private String _strProxyPassword;
    private String _strHostName;
    private String _strDomainName;
    private String _strRealm;

    private void init( )
    {
        _strServerAddress = AppPropertiesService.getProperty( PROPERTY_S2LOW_SERVER );
        _strPort = AppPropertiesService.getProperty( PROPERTY_S2LOW_PORT );
        _strKeyStorePath = AppPropertiesService.getProperty( PROPERTY_S2LOW_KEYSTORE_PATH );
        _strKeyStorePassword = AppPropertiesService.getProperty( PROPERTY_S2LOW_KEYSTORE_PASSWORD );
        _strKeyStoreType = AppPropertiesService.getProperty( PROPERTY_S2LOW_KEYSTORE_TYPE, DEFAULT_STORE_TYPE );
        _strKeyTrustPath = AppPropertiesService.getProperty( PROPERTY_S2LOW_KEYTRUST_PATH );
        _strKeyTrustPassword = AppPropertiesService.getProperty( PROPERTY_S2LOW_KEYTRUST_PASSWORD );
        _strKeyTrustType = AppPropertiesService.getProperty( PROPERTY_S2LOW_KEYTRUST_TYPE, DEFAULT_STORE_TYPE );

        _strProxyHost = AppPropertiesService.getProperty( PROPERTY_S2LOW_PROXY_HOST );
        _strProxyPort = AppPropertiesService.getProperty( PROPERTY_S2LOW_PROXY_PORT );
        _strProxyUserName = AppPropertiesService.getProperty( PROPERTY_S2LOW_PROXY_USERNAME );
        _strProxyPassword = AppPropertiesService.getProperty( PROPERTY_S2LOW_PROXY_PASSWORD );
        _strHostName = AppPropertiesService.getProperty( PROPERTY_S2LOW_HOSTNAME );
        _strDomainName = AppPropertiesService.getProperty( PROPERTY_S2LOW_DOMAIN );
        _strRealm = AppPropertiesService.getProperty( PROPERTY_S2LOW_REALM );

        // Initialisation JSSE
        try
        {
            System.setProperty( "javax.net.ssl.keyStore", _strKeyStorePath );
            System.setProperty( "javax.net.ssl.keyStorePassword", _strKeyStorePassword );
            System.setProperty( "javax.net.ssl.keyStoreType", _strKeyStoreType );
            System.setProperty( "javax.net.ssl.trustStore", _strKeyTrustPath );
            System.setProperty( "javax.net.ssl.trustStorePassword", _strKeyTrustPassword );
            System.setProperty( "javax.net.ssl.trustStoreType", _strKeyTrustType );
        }
        catch( Exception e )
        {
            AppLogService.error( "Erreur de configuration : " + e.getMessage( ), e );
        }
    }

    public String sendActe( Acte acte ) throws TransmissionException
    {
        init( );

        String strIdTransaction;

        // HTTPCLIENT
        HttpClient client = new HttpClient( );

        PostMethod post = new PostMethod( "https://" + _strServerAddress + ":" + _strPort + URL_CREATE );

        // if proxy host and port found, set the correponding elements
        if ( ( _strProxyHost != null ) && ( !_strProxyHost.equals( "" ) ) && ( _strProxyPort != null ) && ( !_strProxyPort.equals( "" ) ) )
        {
            client.getHostConfiguration( ).setProxy( _strProxyHost, Integer.parseInt( _strProxyPort ) );
        }

        Credentials cred = null;

        // if hostname and domain name found, consider we are in NTLM authentication scheme
        // else if only username and password found, use simple UsernamePasswordCredentials
        if ( ( _strHostName != null ) && ( _strDomainName != null ) )
        {
            cred = new NTCredentials( _strProxyUserName, _strProxyPassword, _strHostName, _strDomainName );
        }
        else
            if ( ( _strProxyUserName != null ) && ( _strProxyPassword != null ) )
            {
                cred = new UsernamePasswordCredentials( _strProxyUserName, _strProxyPassword );
            }

        if ( cred != null )
        {
            client.getState( ).setProxyCredentials( _strRealm, _strProxyHost, cred );
            client.getState( ).setAuthenticationPreemptive( true );
            post.setDoAuthentication( true );
        }

        // R�cup�ration des fichiers
        ArrayList<FilePart> fileParts = new ArrayList<FilePart>( );

        try
        {
            // Premier fichier
            File fichierPDF = new File( acte.getDocument( ).getNomFichier( ) );
            fileParts.add( new FilePart( "acte_pdf_file", fichierPDF ) );
        }
        catch( FileNotFoundException e )
        {
            AppLogService.error( "Fichier PDF non trouv� ", e );
            throw new TransmissionException( "Fichier PDF non trouv� ", e );
        }

        /*
         * if (MimetypeMap.MIMETYPE_PDF.equals(mainDocReader.getMimetype())) { byte[] signature = this.parapheurService.getSignature(dossier); if (signature !=
         * null && signature.length > 0) { File ficSign = TempFileProvider.createTempFile("s2low", "p7s"); FileOutputStream os = new FileOutputStream(ficSign);
         * os.write(signature); os.close(); fileParts.add(new FilePart("acte_pdf_file_sign", ficSign)); } }
         */

        // pi�ces jointes en annexe
        try
        {
            for ( FichierSigne fichier : acte.getAnnexes( ).getAnnexe( ) )
            {
                File file = new File( fichier.getNomFichier( ) );
                fileParts.add( new FilePart( "acte_attachments[]", file ) );
            }
        }
        catch( FileNotFoundException e )
        {
            AppLogService.error( "Fichier joint non trouv� ", e );
            throw new TransmissionException( "Fichier joint non trouv� ", e );
        }

        // Remplissage des param�tres POST
        ArrayList<Part> parts = new ArrayList<Part>( );
        parts.add( new StringPart( "api", "1" ) );
        parts.add( new StringPart( "nature_code", acte.getCodeNatureActe( ).toString( ) ) );
        parts.add( new StringPart( "classif1", acte.getCodeMatiere1( ).getCodeMatiere( ).toString( ) ) );

        if ( acte.getCodeMatiere2( ) != null )
        {
            parts.add( new StringPart( "classif2", acte.getCodeMatiere2( ).getCodeMatiere( ).toString( ) ) );
        }

        if ( acte.getCodeMatiere3( ) != null )
        {
            parts.add( new StringPart( "classif3", acte.getCodeMatiere3( ).getCodeMatiere( ).toString( ) ) );
        }

        if ( acte.getCodeMatiere4( ) != null )
        {
            parts.add( new StringPart( "classif4", acte.getCodeMatiere4( ).getCodeMatiere( ).toString( ) ) );
        }

        if ( acte.getCodeMatiere5( ) != null )
        {
            parts.add( new StringPart( "classif5", acte.getCodeMatiere5( ).getCodeMatiere( ).toString( ) ) );
        }

        try
        {
            parts.add( new StringPart( "number", new String( acte.getNumeroInterne( ).getBytes( ), "ISO-8859-1" ) ) );

            Calendar calendar = acte.getDate( );
            String strDateDecision = String.format( "%1$04d-%2$02d-%3$02d", calendar.get( Calendar.YEAR ), calendar.get( Calendar.MONTH ) + 1,
                    calendar.get( Calendar.DAY_OF_MONTH ) );

            parts.add( new StringPart( "decision_date", new String( strDateDecision.getBytes( ), "ISO-8859-1" ) ) );
            parts.add( new StringPart( "subject", new String( acte.getObjet( ).getBytes( ), "ISO-8859-1" ) ) );
        }
        catch( UnsupportedEncodingException e )
        {
            AppLogService.error( "Erreur d'encoding ", e );
        }

        for ( FilePart ficPart : fileParts )
            parts.add( ficPart );

        Part [ ] partsTab = new Part [ parts.size( )];

        for ( int i = 0; i < parts.size( ); i++ )
            partsTab [i] = parts.get( i );

        post.setRequestEntity( new MultipartRequestEntity( partsTab, post.getParams( ) ) );

        // Ex�cution de la m�thode POST
        try
        {
            int status = client.executeMethod( post );

            if ( HttpStatus.SC_OK == status )
            {
                String reponse = post.getResponseBodyAsString( );
                String [ ] tab = reponse.split( "\n" );

                if ( ( tab.length == 1 ) || "KO".equals( tab [0] ) )
                {
                    String strError = "Erreur retourn�e par la plate-forme s2low : ";

                    for ( int i = 1; i < tab.length; i++ )
                        strError += tab [i];

                    AppLogService.error( strError );
                    throw new TransmissionException( strError );
                }

                // La transaction s'est bien pass�e, on renvoie son identifiant
                strIdTransaction = tab [1];
            }
            else
            {
                String strMessage = "Echec de la r�cup�ration de la connexion � la plate-forme : statut = " + status;
                AppLogService.error( strMessage );
                throw new TransmissionException( strMessage );
            }
        }
        catch( NumberFormatException e )
        {
            AppLogService.error( e.getMessage( ), e );
            throw new TransmissionException( "Erreur " + e.getMessage( ), e );
        }
        catch( IOException e )
        {
            AppLogService.error( e.getMessage( ), e );
            throw new TransmissionException( "Erreur " + e.getMessage( ), e );
        }
        finally
        {
            post.releaseConnection( );
        }

        return strIdTransaction;
    }

    public void getInfos( Transaction transaction ) throws TransmissionException
    {
        init( );

        HttpClient client = new HttpClient( );
        GetMethod get = new GetMethod( "https://" + _strServerAddress + ":" + _strPort + "/modules/actes/actes_transac_get_status.php?transaction="
                + transaction.getCode( ) );

        // Ex�cution de la m�thode GET
        try
        {
            int status = client.executeMethod( get );

            if ( HttpStatus.SC_OK == status )
            {
                String reponse = get.getResponseBodyAsString( );
                String [ ] tab = reponse.split( "\n" );

                if ( ( tab.length == 1 ) || "KO".equals( tab [0] ) )
                {
                    String error = "Erreur retourn�e par la plate-forme s2low : ";

                    for ( int i = 1; i < tab.length; i++ )
                        error += tab [i];

                    throw new RuntimeException( error );
                }

                // La transaction s'est bien pass�e, on renvoie le statut
                int codeRetour = Integer.parseInt( tab [1] );

                switch( codeRetour )
                {
                    case -1:
                        transaction.setStatus( Transaction.STATUS_ERROR );

                        break;

                    case 0:
                        transaction.setStatus( Transaction.STATUS_ANNULE );

                        break;

                    case 1:
                        transaction.setStatus( Transaction.STATUS_POSTE );

                        break;

                    case 2:
                        transaction.setStatus( Transaction.STATUS_EN_ATTENTE );

                        break;

                    case 3:
                        transaction.setStatus( Transaction.STATUS_TRANSMIS );

                        break;

                    case 4:
                        transaction.setStatus( Transaction.STATUS_ACQUITE );

                        break;

                    case 5:
                        transaction.setStatus( Transaction.STATUS_VALIDE );

                        break;

                    case 6:
                        transaction.setStatus( Transaction.STATUS_REFUSE );

                        break;
                }
            }
            else
            {
                throw new TransmissionException( "Echec de la r�cup�ration de la connexion � la plate-forme : statut = " + status );
            }
        }
        catch( IOException e )
        {
            AppLogService.error( e.getMessage( ), e );
            throw new TransmissionException( "Erreur " + e.getMessage( ), e );
        }
        finally
        {
            get.releaseConnection( );
        }
    }

    /*
     * public String getInfosS2low() throws IOException { String res = null; String strServerAddress = AppPropertiesService.getProperty( PROPERTY_S2LOW_SERVER
     * ); String strPort = AppPropertiesService.getProperty( PROPERTY_S2LOW_PORT ); String strCertificat = AppPropertiesService.getProperty(
     * PROPERTY_S2LOW_CERTIFICAT ); String strPassword = AppPropertiesService.getProperty( PROPERTY_S2LOW_PASSWORD );
     * 
     * // Initialisation des objets HttpClient // COMMONS-SSL try { EasySSLProtocolSocketFactory easy = new EasySSLProtocolSocketFactory(); KeyMaterial km = new
     * KeyMaterial( strCertificat , strPassword.toCharArray()); easy.setKeyMaterial(km); Protocol easyhttps = new Protocol("https", (ProtocolSocketFactory)easy,
     * Integer.parseInt( strPort )); Protocol.registerProtocol("https", easyhttps); } catch (Exception e) {
     * logger.warn("Erreur lors de la modification du protocole https"); logger.warn(e.getStackTrace()); }
     * 
     * // HTTPCLIENT HttpClient client = new HttpClient(); GetMethod get = new GetMethod("https://" + serverAddress + ":" + port +
     * "/modules/actes/actes_transac_get_status.php?transaction=" + this.nodeService.getProperty(dossier, ParapheurModel.PROP_TRANSACTION_ID));
     * 
     * // Ex�cution de la m�thode GET try { int status = client.executeMethod(get); if (HttpStatus.SC_OK == status) { String reponse =
     * get.getResponseBodyAsString(); String[] tab = reponse.split("\n"); if (tab.length == 1 || "KO".equals(tab[0])) { String error =
     * "Erreur retourn�e par la plate-forme s2low : "; for (int i=1; i<tab.length; i++) error += tab[i]; throw new RuntimeException(error); }
     * 
     * // La transaction s'est bien pass�e, on renvoie le statut int codeRetour = Integer.parseInt(tab[1]); switch (codeRetour) { case -1 : res = "Erreur";
     * break; case 0 : res = "Annul�"; break; case 1 : res = "Post�"; break; case 2 : res = "En attente de transmission"; break; case 3 : res = "Transmis";
     * break; case 4 : res = "Acquittement re�u"; break; case 5 : res = "Valid�"; break; case 6 : res = "Refus�"; break; }
     * 
     * // On enregistre le statut renvoy� this.nodeService.setProperty(dossier, ParapheurModel.PROP_STATUS, res); } else { throw new
     * RuntimeException("Echec de la r�cup�ration de la connexion � la plate-forme : statut = " + status); } } finally { get.releaseConnection(); }
     * 
     * return res; }
     */

    /**
     * @see com.atolcd.parapheur.repo.ParapheurService#envoiS2low(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */

    /*
     * public void envoiS2low(NodeRef dossier, String nature, String classification, String numero, String objet, String date) throws IOException {
     * Assert.isTrue(this.parapheurService.isDossier(dossier),"Node Ref doit �tre de type ph:dossier"); Assert.isTrue(this.parapheurService.isTermine(dossier),
     * "Le dossier n'est pas termin�"); Assert.isTrue(!this.nodeService.hasAspect(dossier, ParapheurModel.ASPECT_S2LOW),
     * "Le dossier a d�j� �t� envoy� � la plate-forme S2LOW");
     * 
     * String serverAddress = this.configuration.getProperty("s2low.server"); String port = this.configuration.getProperty("s2low.port");
     * 
     * // Initialisation des objets HttpClient // COMMONS-SSL try { EasySSLProtocolSocketFactory easy = new EasySSLProtocolSocketFactory(); KeyMaterial km = new
     * KeyMaterial(this.configuration.getProperty("s2low.certificat"), this.configuration.getProperty("s2low.password").toCharArray()); easy.setKeyMaterial(km);
     * Protocol easyhttps = new Protocol("https", (ProtocolSocketFactory)easy, Integer.parseInt(port)); Protocol.registerProtocol("https", easyhttps); } catch
     * (Exception e) { logger.warn("Erreur lors de la modification du protocole https"); logger.warn(e.getStackTrace()); }
     * 
     * // HTTPCLIENT HttpClient client = new HttpClient(); PostMethod post = new PostMethod("https://" + serverAddress + ":" + port +
     * "/modules/actes/actes_transac_create.php");
     * 
     * // R�cup�ration des fichiers List<NodeRef> documents = this.parapheurService.getDocuments(dossier); ArrayList<FilePart> fileParts = new
     * ArrayList<FilePart>(); // Premier fichier ContentReader mainDocReader = this.contentService.getReader(documents.get(0), ContentModel.PROP_CONTENT); File
     * ficPdf = ensureMimeType(mainDocReader, MimetypeMap.MIMETYPE_PDF); fileParts.add(new FilePart("acte_pdf_file", ficPdf)); if
     * (MimetypeMap.MIMETYPE_PDF.equals(mainDocReader.getMimetype())) { byte[] signature = this.parapheurService.getSignature(dossier); if (signature != null &&
     * signature.length > 0) { File ficSign = TempFileProvider.createTempFile("s2low", "p7s"); FileOutputStream os = new FileOutputStream(ficSign);
     * os.write(signature); os.close(); fileParts.add(new FilePart("acte_pdf_file_sign", ficSign)); } }
     * 
     * // Suivants for (int i=1; i<documents.size(); i++) { NodeRef doc = documents.get(i); File ficPdfx = TempFileProvider.createTempFile("s2low", null);
     * this.contentService.getReader(doc, ContentModel.PROP_CONTENT).getContent(ficPdfx); fileParts.add(new FilePart("acte_attachments[]", ficPdfx)); }
     * 
     * // Lecture de la classification ArrayList<StringPart> classifParts = new ArrayList<StringPart>(); String[] classifications = classification.split("-");
     * for (int i=1; i<=classifications.length; i++) { classifParts.add(new StringPart("classif"+i, classifications[i-1])); }
     * 
     * // Remplissage des param�tres POST ArrayList<Part> parts = new ArrayList<Part>(); parts.add(new StringPart("api", "1")); parts.add(new
     * StringPart("nature_code", nature)); for (StringPart classif : classifParts) parts.add(classif); parts.add(new StringPart("number", new String
     * (numero.getBytes(), "ISO-8859-1" ))); parts.add(new StringPart("decision_date", new String (date.getBytes(), "ISO-8859-1" ))); parts.add(new
     * StringPart("subject", new String (objet.getBytes(), "ISO-8859-1" ))); for (FilePart ficPart : fileParts) parts.add(ficPart);
     * 
     * 
     * Part[] partsTab = new Part[parts.size()]; for (int i=0; i<parts.size(); i++) partsTab[i] = parts.get(i);
     * 
     * post.setRequestEntity(new MultipartRequestEntity(partsTab, post.getParams())); // Ex�cution de la m�thode POST try { int status =
     * client.executeMethod(post); if (HttpStatus.SC_OK == status) { String reponse = post.getResponseBodyAsString(); String[] tab = reponse.split("\n"); if
     * (tab.length == 1 || "KO".equals(tab[0])) { String error = "Erreur retourn�e par la plate-forme s2low : "; for (int i=1; i<tab.length; i++) error +=
     * tab[i]; throw new RuntimeException(error); }
     * 
     * // La transaction s'est bien pass�e, on renvoie son identifiant int res = Integer.parseInt(tab[1]); // On enregistre le num�ro de transaction attribu�
     * par la plate-forme Map<QName, Serializable> pptes = new HashMap<QName, Serializable>(); pptes.put(ParapheurModel.PROP_TRANSACTION_ID, res);
     * this.nodeService.addAspect(dossier, ParapheurModel.ASPECT_S2LOW, pptes); } else { throw new
     * RuntimeException("Echec de la r�cup�ration de la connexion � la plate-forme : statut = " + status); } } finally { post.releaseConnection(); } }
     * 
     * private File ensureMimeType(ContentReader reader, String mimeType) { File fic = TempFileProvider.createTempFile("s2low", null); // Si le type MIME
     * correspond, on l'envoie tel quel if(mimeType.equals(reader.getMimetype())) { reader.getContent(fic); } // Sinon, on commence par le transformer else {
     * FileContentWriter tmpWriter = new FileContentWriter(fic); tmpWriter.setMimetype(mimeType); tmpWriter.setEncoding(reader.getEncoding());
     * this.contentService.transform(reader, tmpWriter); }
     * 
     * return fic; }
     */
}
