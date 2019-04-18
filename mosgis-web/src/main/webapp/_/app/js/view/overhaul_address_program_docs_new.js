define ([], function () {

    var name = 'overhaul_address_program_docs_new_form'

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: name,

                record: data.record,

                fields : [                    
                    {name: 'code_nsi_79', type: 'list', options: {items: data.vc_nsi_79.items}},
                    {name: 'number_', type: 'text'},
                    {name: 'date_', type: 'date'},
                    {name: 'fullname', type: 'text'},
                    {name: 'legislature', type: 'text'}
                ],
                                                        
            })

       })

    }

})