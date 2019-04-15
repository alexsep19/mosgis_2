define ([], function () {

    var name = 'overhaul_short_programs_new_form'

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: name,

                record: data.record,

                fields : [                    
                    {name: 'programname', type: 'text'},
                    {name: 'startyear', type: 'text'},
                    {name: 'startmonth', type: 'text'},
                    {name: 'endyear', type: 'text'},
                    {name: 'endmonth', type: 'text'}
                ],
                                                        
            })

       })

    }

})