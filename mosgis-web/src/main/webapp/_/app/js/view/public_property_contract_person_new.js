define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'public_property_contract_form',

                record: data.record,

                fields : [                                

                    {name: 'contractnumber', type: 'text'},
                    {name: 'uuid_person_customer', type: 'list', options: {items: data.persons}},

                    {name: 'date_', type: 'date'},
                    {name: 'enddate', type: 'date'},
                    {name: 'startdate', type: 'date'},
                    
                    {name: 'fiashouseguid', type: 'list', options: {items: data.houses}},

                ],
                
//                focus: 2,
                
            })
            
       })

    }

})