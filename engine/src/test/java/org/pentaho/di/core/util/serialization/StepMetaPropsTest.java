/*
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 * **************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pentaho.di.core.util.serialization;

import com.google.common.base.Objects;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.streaming.common.BaseStreamStepMeta;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class StepMetaPropsTest {


  @InjectionSupported ( localizationPrefix = "stuff", groups = { "stuffGroup" } ) static class FooMeta
    extends BaseStreamStepMeta {

    @Sensitive
    @Injection ( name = "FIELD1", group = "stuffGroup" ) String field1 = "default";
    @Injection ( name = "FIELD2", group = "stuffGroup" ) int field2 = 123;

    @Sensitive
    @Injection ( name = "PassVerd" ) String password = "should.be.encrypted";


    @Injection ( name = "ALIST" ) List<String> alist = new ArrayList<>();

    @Sensitive
    @Injection ( name = "SECURELIST" ) List<String> securelist = new ArrayList<>();

    @Injection ( name = "BOOLEANLIST" ) List<Boolean> blist = new ArrayList<>();
    @Injection ( name = "IntList" ) List<Integer> ilist = new ArrayList<>();


    @Override
    public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
                                  TransMeta transMeta,
                                  Trans trans ) {
      return null;
    }

    @Override public StepDataInterface getStepData() {
      return null;
    }

    @Override public boolean equals( Object o ) {
      if ( this == o ) {
        return true;
      }
      if ( o == null || getClass() != o.getClass() ) {
        return false;
      }
      FooMeta fooMeta = (FooMeta) o;
      return field2 == fooMeta.field2
        && Objects.equal( field1, fooMeta.field1 )
        && Objects.equal( alist, fooMeta.alist )
        && Objects.equal( blist, fooMeta.blist )
        && Objects.equal( ilist, fooMeta.ilist );
    }

    @Override public int hashCode() {
      return Objects.hashCode( field1, field2, alist );
    }

    @Override public String toString() {
      return String
        .format( "FooMeta{%nfield1='%s', %nfield2=%d, %nalist=%s, %nblist=%s, %nilist=%s}",
          field1, field2, alist, blist, ilist );
    }

    @Override public RowMeta getRowMeta( String origin, VariableSpace space ) throws KettleStepException {
      return null;
    }
  }


  @Before
  public void before() throws KettleException {
    KettleEnvironment.init();
  }

  @Test
  public void testToAndFrom() {
    FooMeta foo = getTestFooMeta();

    StepMetaProps fromMeta = StepMetaProps.from( foo );

    FooMeta toMeta = new FooMeta();
    fromMeta.to( toMeta );

    assertThat( foo, equalTo( toMeta ) );
  }

  @Test
  public void testEncrypt() {
    FooMeta foo = getTestFooMeta();
    foo.password = "p@ssword";
    StepMetaProps stepMetaProps = StepMetaProps.from( foo );

    assertThat( "password field should be encrypted, so should not be present in the .toString of the props",
      stepMetaProps.toString(), not( containsString( "p@ssword" ) ) );

    FooMeta toMeta = new FooMeta();
    stepMetaProps.to( toMeta );

    assertThat( foo, equalTo( toMeta ) );
    assertThat( "p@ssword", equalTo( toMeta.password ) );
  }

  @Test
  public void testEncryptedList() {
    FooMeta foo = getTestFooMeta();

    foo.securelist = asList( "shadow", "substance" );
    StepMetaProps stepMetaProps = StepMetaProps.from( foo );


    assertThat(
      "secureList should be encrypted, so raw values should not be present in the .toString of the props",
      stepMetaProps.toString(), not( containsString( "expectedString" ) ) );
    asList( "shadow", "substance" ).forEach( val ->
      assertThat( val + " should be encrypted, so should not be present in the .toString of the props",
        stepMetaProps.toString(), not( containsString( val ) ) ) );

    FooMeta toMeta = new FooMeta();
    stepMetaProps.to( toMeta );

    assertThat( foo, equalTo( toMeta ) );
    assertThat( asList( "shadow", "substance" ), equalTo( toMeta.securelist ) );
  }


  static FooMeta getTestFooMeta() {
    FooMeta foo = new FooMeta();

    foo.field1 = "expectedString";
    foo.field2 = 42;
    foo.alist = asList( "one", "two", "three", "four" );
    foo.blist = asList( true, false, false );
    foo.ilist = asList( 1, 4, 26 );
    return foo;
  }


}
