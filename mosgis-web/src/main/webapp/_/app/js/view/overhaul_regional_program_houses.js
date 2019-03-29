define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['topmost_layout'].el ('main')).w2regrid ({ 

            name: 'overhaul_regional_program_houses_grid',

            show: {
                toolbar: true,
                toolbarInput: false,
                toolbarAdd: data.item._can.edit && ($_USER.role.nsi_20_7 || $_USER.role.admin),
                footer: true,
            },

            columns: [
                {field: 'address', caption: 'Адрес', size: 10},
                {field: 'oktmo', caption: 'Код ОКТМО', size: 10},
            ],
            
            url: '/mosgis/_rest/?type=overhaul_regional_program_houses',

            postData: {program_uuid: $_REQUEST.id},

            onAdd: $_DO.create_overhaul_regional_program_house,
            
            onDblClick: function (e) {
                openTab ('/overhaul_regional_program_house/' + e.recid)
            },

        }).refresh ();

    }

})