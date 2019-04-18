define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['overhauls_layout'].el ('main')).w2regrid ({

            name: 'overhaul_address_programs_grid',

            show: {
                toolbar: true,
                toolbarInput: false,
                toolbarAdd: $_USER.role.nsi_20_7 || $_USER.role.admin,
                footer: true,
            },

            columns: [
                {field: 'programname', caption: 'Наименование', size: 10},
                {field: 'startmonthyear', caption: 'Дата начала', size: 10, render: _dt},
                {field: 'endmonthyear', caption: 'Дата окончания', size: 10, render: _dt},
                {field: 'id_oap_status', caption: 'Статус', size: 10, voc: data.vc_gis_status},
            ],
            
            url: '/_back/?type=overhaul_address_programs',

            onAdd: $_DO.create_overhaul_address_program,
            
            onDblClick: function (e) {
                openTab ('/overhaul_address_program/' + e.recid)
            },

        }).refresh ();

    }

})