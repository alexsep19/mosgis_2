define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'org_work_form',

                record: {},

                fields : [                                
                ],

            })

            
            $('#required_services_container').w2regrid ({ 
            
                name: 'new_objects_grid',
                
                show: {
                    toolbar: false,
                    footer: false,
                    columnHeaders: true,
                    selectColumn: true
                },     
                
                columns: [
                    {field: 'label', caption: 'Адрес', size: 50},
                    {field: 'startdate', caption: 'С', size: 10, render: _dt},
                    {field: 'enddate', caption: 'По', size: 10, render: _dt},
                ],
                
                records: data.records,
                            
            }).refresh ()

       })

    }

})