package com.solutis.dev.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import com.solutis.dev.infrastructure.config.DataSourceConfig.FailoverDataSource;

class DataSourceConfigTest {

    @Test
    void getConnection_shouldUsePrimaryDataSource_whenPrimaryIsAvailable() throws SQLException {
        DataSource primary = mock(DataSource.class);
        DataSource fallback = mock(DataSource.class);
        Connection primaryConnection = mock(Connection.class);
        when(primary.getConnection()).thenReturn(primaryConnection);

        FailoverDataSource failoverDataSource = new FailoverDataSource(primary, fallback);
        Connection result = failoverDataSource.getConnection();

        assertThat(result).isSameAs(primaryConnection);
        verify(fallback, never()).getConnection();
    }

    @Test
    void getConnection_shouldFallBackToSecondaryDataSource_whenPrimaryConnectionFails() throws SQLException {
        DataSource primary = mock(DataSource.class);
        DataSource fallback = mock(DataSource.class);
        Connection fallbackConnection = mock(Connection.class);
        when(primary.getConnection()).thenThrow(new SQLException("Postgres indisponível"));
        when(fallback.getConnection()).thenReturn(fallbackConnection);

        FailoverDataSource failoverDataSource = new FailoverDataSource(primary, fallback);
        Connection result = failoverDataSource.getConnection();

        assertThat(result).isSameAs(fallbackConnection);
    }

    @Test
    void getConnectionWithCredentials_shouldFallBackToSecondaryDataSource_whenPrimaryConnectionFails()
            throws SQLException {
        DataSource primary = mock(DataSource.class);
        DataSource fallback = mock(DataSource.class);
        Connection fallbackConnection = mock(Connection.class);
        when(primary.getConnection("user", "pass")).thenThrow(new SQLException("Postgres indisponível"));
        when(fallback.getConnection("user", "pass")).thenReturn(fallbackConnection);

        FailoverDataSource failoverDataSource = new FailoverDataSource(primary, fallback);
        Connection result = failoverDataSource.getConnection("user", "pass");

        assertThat(result).isSameAs(fallbackConnection);
    }

    @Test
    void getConnectionWithCredentials_shouldUsePrimaryDataSource_whenPrimaryIsAvailable() throws SQLException {
        DataSource primary = mock(DataSource.class);
        DataSource fallback = mock(DataSource.class);
        Connection primaryConnection = mock(Connection.class);
        when(primary.getConnection("user", "pass")).thenReturn(primaryConnection);

        FailoverDataSource failoverDataSource = new FailoverDataSource(primary, fallback);
        Connection result = failoverDataSource.getConnection("user", "pass");

        assertThat(result).isSameAs(primaryConnection);
        verify(fallback, never()).getConnection("user", "pass");
    }
}
