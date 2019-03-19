define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['topmost_layout'].el ('main')).w2regrid ({ 

            name: 'overhaul_regional_programs_grid',

            show: {
                toolbar: true,
                toolbarInput: false,
                toolbarAdd: $_USER.role.nsi_20_7 || $_USER.role.admin,
                footer: true,
            },

            columns: [
                {field: 'programname', caption: 'Наименование', size: 10},
                {field: 'startyear', caption: 'Год начала', size: 10},
                {field: 'endyear', caption: 'Год окончания', size: 10},
                {field: 'id_orp_status', caption: 'Статус', size: 10, voc: data.vc_gis_status},
            ],
            
            url: '/mosgis/_rest/?type=overhaul_regional_programs',

            onAdd: $_DO.create_overhaul_regional_program,
            
            onDblClick: function (e) {
                openTab ('/overhaul_regional_program/' + e.recid)
            },

        }).refresh ();

    }

})