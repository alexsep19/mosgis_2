define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')

        function canAdd () {
            
            if (!data.item.is_deleted && ($_USER.role.nsi_20_7 || $_USER.role.admin)) {
                switch (data.item.last_succesfull_status) {
                    case  10:
                    case -22:
                        return true
                }
            }
            return false

        }
        
        $(w2ui ['topmost_layout'].el ('main')).w2regrid ({ 

            name: 'overhaul_regional_program_houses_grid',

            show: {
                toolbar: true,
                toolbarInput: false,
                toolbarAdd: canAdd (),
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