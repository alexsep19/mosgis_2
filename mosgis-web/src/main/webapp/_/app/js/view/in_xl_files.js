define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['integration_layout'].el ('main')).w2regrid ({ 

            name: 'voc_organizations_grid',

            show: {
                toolbar: true,
                toolbarSearch: false,
                toolbarInput: false,
                footer: true,
            },     
            
            columns: [                
                {field: 'id_type', caption: 'Тип',    size: 25, voc: data.vc_xl_file_types},
                {field: 'ts', caption: 'Дата начала',    size: 16, render: _ts},
                {field: 'log.ts', caption: 'Дата окончания',    size: 16, render: _ts},
                
                {field: 'u.label', caption: 'Пользователь',    size: 20},
                {field: 'org.label', caption: 'Организация',    size: 20, off: !$_USER.role.admin},
                
                {field: 'label', caption: 'Исходный файл',    size: 20, attr: 'data-ref=1'},
                {field: 'label_result', caption: 'Обработанный файл', attr: 'data-ref=1', size: 20},
                {field: 'id_status', caption: 'Статус обработки',    size: 20, voc: data.vc_file_status},

            ].filter (not_off),
            
            postData: {data: {uuid_org: $_REQUEST.id}},

            url: '/_back/?type=in_xl_files',

            onDblClick: function (e) {},

            onClick: function (e) {

                var col = this.columns [e.column].field

                if (!this.get (e.recid) [col]) return

                switch (col) {
                    case 'label': return $_DO.download_in_xl_files (e)
                    case 'label_result': return $_DO.download_errors_in_xl_files (e)
                }

            },

        }).refresh ();

    }

})