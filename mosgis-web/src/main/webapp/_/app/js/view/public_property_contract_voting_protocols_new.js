define ([], function () {

    return function (data, view) {

        $(fill (view, data)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'org_work_form',

                record: {},

                fields : [                                
                ],

            })

            
            $('#required_services_container').w2regrid ({ 
            
                name: 'new_org_works_grid',
                
                show: {
                    toolbar: false,
                    footer: false,
                    columnHeaders: true,
                    selectColumn: true
                },     
                
                columns: [
                    {field: 'label', caption: 'Наименование', size: 50},
                ],
                
                records: data.records,
                            
            }).refresh ()

       })

    }

})